package com.jobboard.jobboard.service;

import com.jobboard.jobboard.model.Job;
import com.jobboard.jobboard.repository.JobRepository;
import com.jobboard.jobboard.dto.RemotiveJobDTO;
import com.jobboard.jobboard.dto.RemotiveResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Service
@RequiredArgsConstructor
public class JobScraperService {

    private final JobRepository jobRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String REMOTIVE_URL = "https://remotive.io/api/remote-jobs";
    private final JobService jobService;

    public int scrapeAndSaveJobs() {
        RemotiveResponse response = restTemplate.getForObject(REMOTIVE_URL, RemotiveResponse.class);

        if (response == null || response.getJobs() == null || response.getJobs().isEmpty()) {
            System.out.println("No jobs received from Remotive.");
            return 0;
        }

        List<RemotiveJobDTO> dtos = response.getJobs();
        System.out.println("Total jobs fetched: " + dtos.size());

        dtos.stream().limit(3).forEach(dto -> System.out.println(dto.getTitle() + " at " + dto.getCompany_name()));

        List<Job> jobs = dtos.stream()
                .limit(100)
                .map(dto -> {
                    String description = Jsoup.parse(dto.getDescription()).text();
                    String salary = dto.getSalary();
                    
                    if (salary == null || salary.isBlank()) {
                        salary = extractSalaryFromDescription(description);
                    }
                    
                    Job job = new Job();
                    job.setTitle(dto.getTitle());
                    job.setCompanyName(dto.getCompany_name());
                    job.setLocation(dto.getCandidate_required_location());
                    job.setUrl(dto.getUrl());
                    job.setDescription(description);
                    job.setSalary(salary);
                    job.setTags(String.join(",", dto.getTags()));
                    normalizeSalaryFields(job);
                    return job;
                }).collect(Collectors.toList());

        jobRepository.saveAll(jobs);
        cleanupIfJobCountExceedsLimit();
        
        // Clear caches after scraping
        jobService.clearCache();
        
        return jobs.size();
    }

    private void cleanupIfJobCountExceedsLimit() {
        long count = jobRepository.count();
        int limit = 300;

        if (count > limit) {
            int toDelete = (int) (count - limit);
            List<Job> oldJobs = jobRepository.findOldestJobs(toDelete);
            jobRepository.deleteAll(oldJobs);
            System.out.println("Deleted " + toDelete + " old jobs to keep DB clean.");
        }
    }
    
    private String extractSalaryFromDescription(String description) {
        if (description == null || description.isEmpty()) {
            return null;
        }

        // First, look for salary-related sections in the description
        String[] salaryKeywords = {
            "salary",
            "compensation",
            "pay",
            "remuneration",
            "package",
            "earnings"
        };

        // Try to find a salary section first
        for (String keyword : salaryKeywords) {
            String pattern = "(?i)" + keyword + "[^.]*\\$[0-9,]+(?:k|K)?(?:\\s*-\\s*\\$[0-9,]+(?:k|K)?)?";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(description);
            if (m.find()) {
                return m.group(0);
            }
        }

        // If no salary section found, look for any number that might be a salary
        String[] generalPatterns = {
            "\\$[0-9,]+(?:k|K)?(?:\\s*-\\s*\\$[0-9,]+(?:k|K)?)?",  // $60k-$80k or $70k
            "[0-9,]+(?:k|K)?(?:\\s*-\\s*[0-9,]+(?:k|K)?)?",        // 60k-80k or 70k
            "from\\s+\\$[0-9,]+(?:k|K)?",                          // from $70k
            "up\\s+to\\s+\\$[0-9,]+(?:k|K)?",                      // up to $80k
            "base\\s+salary\\s+of\\s+\\$[0-9,]+(?:k|K)?",          // base salary of $70k
            "total\\s+compensation\\s+of\\s+\\$[0-9,]+(?:k|K)?",   // total compensation of $70k
            "annual\\s+salary\\s+of\\s+\\$[0-9,]+(?:k|K)?",        // annual salary of $70k
            "starting\\s+at\\s+\\$[0-9,]+(?:k|K)?",                // starting at $70k
            "range\\s+of\\s+\\$[0-9,]+(?:k|K)?\\s*-\\s*\\$[0-9,]+(?:k|K)?", // range of $60k-$80k
            "between\\s+\\$[0-9,]+(?:k|K)?\\s+and\\s+\\$[0-9,]+(?:k|K)?"    // between $60k and $80k
        };

        for (String pattern : generalPatterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher m = p.matcher(description);
            if (m.find()) {
                return m.group(0);
            }
        }

        return null;
    }

    /**
     * Parse job.getSalary() (e.g. "$60k-$80k" or "70k") and set minSalary/maxSalary.
     */
    private void normalizeSalaryFields(Job job) {
        String raw = job.getSalary();
        if (raw == null || raw.isBlank()) {
            job.setMinSalary(null);
            job.setMaxSalary(null);
            return;
        }

        // strip dollar signs, commas, whitespace
        String cleaned = raw.replaceAll("[$,]", "").trim();

        // 1) try a range: "60k-80k" or "60000-80000"
        Pattern range = Pattern.compile("(?i)([0-9]+(?:\\.[0-9]+)?)(k?)\\s*-\\s*([0-9]+(?:\\.[0-9]+)?)(k?)");
        Matcher m = range.matcher(cleaned);
        if (m.find()) {
            double low  = parseNumberWithSuffix(m.group(1), m.group(2));
            double high = parseNumberWithSuffix(m.group(3), m.group(4));
            job.setMinSalary(low);
            job.setMaxSalary(high);
            return;
        }

        // 2) fallback to single value: "70k" or "70000"
        Pattern single = Pattern.compile("(?i)([0-9]+(?:\\.[0-9]+)?)(k?)");
        m = single.matcher(cleaned);
        if (m.find()) {
            double v = parseNumberWithSuffix(m.group(1), m.group(2));
            job.setMinSalary(v);
            job.setMaxSalary(v);
        }
    }

    private double parseNumberWithSuffix(String num, String suffix) {
        double v = Double.parseDouble(num);
        if ("k".equalsIgnoreCase(suffix)) {
            v *= 1_000;
        }
        return v;
    }
}
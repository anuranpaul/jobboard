package com.jobboard.jobboard.service;

import com.jobboard.jobboard.model.Job;
import com.jobboard.jobboard.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;
import com.jobboard.jobboard.util.SalaryParser;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JobService {

    @Autowired
    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {    
        this.jobRepository = jobRepository;
    }

    public List<Job> searchJobs(String keyword, String location, String tag, String minSalary) {
        // Clean and normalize the search parameters
        String normalizedKeyword = normalizeSearchTerm(keyword);
        String normalizedLocation = normalizeSearchTerm(location);
        String normalizedTag = normalizeSearchTerm(tag);
        
        // Parse and normalize the minimum salary
        Double normalizedMinSalary = null;
        if (minSalary != null && !minSalary.trim().isEmpty()) {
            SalaryParser.SalaryRange salaryRange = SalaryParser.parseSalary(minSalary);
            normalizedMinSalary = salaryRange.getMin();
        }

        // Log search parameters for debugging
        log.debug("Search parameters - keyword: {}, location: {}, tag: {}, minSalary: {}", 
            normalizedKeyword, normalizedLocation, normalizedTag, normalizedMinSalary);

        List<Job> results = jobRepository.searchJobs(
                normalizedKeyword,
                normalizedLocation,
                normalizedTag,
                normalizedMinSalary);

        log.debug("Found {} jobs matching the criteria", results.size());
        return results;
    }

    private String normalizeSearchTerm(String term) {
        if (term == null || term.trim().isEmpty()) {
            return null;
        }
        
        // Remove extra whitespace and convert to lowercase
        return term.trim().toLowerCase();
    }
}

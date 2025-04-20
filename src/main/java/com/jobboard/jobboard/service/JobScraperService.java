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
@Service
@RequiredArgsConstructor
public class JobScraperService {

    private final JobRepository jobRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String REMOTIVE_URL = "https://remotive.io/api/remote-jobs";

    public int scrapeAndSaveJobs() {
        RemotiveResponse response = restTemplate.getForObject(REMOTIVE_URL, RemotiveResponse.class);

        if (response == null || response.getJobs() == null || response.getJobs().isEmpty()) {
            System.out.println("No jobs received from Remotive.");
            return 0;
        }

        List<RemotiveJobDTO> dtos = response.getJobs();
        System.out.println("Total jobs fetched: " + dtos.size());

        // Print first 3 jobs
        dtos.stream().limit(3).forEach(dto -> System.out.println(dto.getTitle() + " at " + dto.getCompany_name()));

        List<Job> jobs = dtos.stream()
            .limit(100)
            .map(dto -> {
            Job job = new Job();
            job.setTitle(dto.getTitle());
            job.setCompanyName(dto.getCompany_name());
            job.setLocation(dto.getCandidate_required_location());
            job.setUrl(dto.getUrl());
            job.setDescription(dto.getDescription());
            job.setSalary(dto.getSalary());
            job.setTags(String.join(",", dto.getTags()));
            return job;
        }).collect(Collectors.toList());

        jobRepository.saveAll(jobs);
        cleanupIfJobCountExceedsLimit();
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

}

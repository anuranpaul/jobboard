package com.jobboard.jobboard.controller;

import com.jobboard.jobboard.model.Job;
import com.jobboard.jobboard.repository.JobRepository;
import com.jobboard.jobboard.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.jobboard.jobboard.dto.ApiResponse;
import com.jobboard.jobboard.exceptions.NotFoundException;
import com.jobboard.jobboard.constants.Messages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.jobboard.jobboard.dto.PagedResponse;
import com.jobboard.jobboard.dto.JobSearchRequest;
import org.springframework.data.domain.PageRequest;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
@Slf4j
public class JobController {

    private final JobService jobService;
    private final JobRepository jobRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<Job>>> getJobs(
            @Valid JobSearchRequest searchRequest,
            Pageable pageable) {
        
        // Validate page size
        int maxPageSize = 100;
        if (pageable.getPageSize() > maxPageSize) {
            pageable = PageRequest.of(
                pageable.getPageNumber(),
                maxPageSize,
                pageable.getSort()
            );
        }
        
        log.debug("Searching jobs with validated parameters - request: {}, pageable: {}", 
            searchRequest, pageable);
            
        Page<Job> jobsPage = jobService.searchJobs(
            searchRequest.getKeyword(), 
            searchRequest.getLocation(),
            searchRequest.getTag(),
            searchRequest.getMinSalary(),
            pageable
        );
        
        PagedResponse<Job> response = PagedResponse.from(jobsPage);
        
        if (jobsPage.isEmpty()) {
            log.info("No jobs found for search criteria");
            return ResponseEntity.ok(ApiResponse.ok(Messages.NO_JOBS_FOUND, response));
        }
        
        return ResponseEntity.ok(ApiResponse.ok(Messages.JOBS_FOUND, response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Job>> getJobById(@PathVariable Long id) {
        Job job = jobService.getJobById(id);
        if (job == null) {
            throw new NotFoundException(Messages.JOB_NOT_FOUND + id);
        }
        return ResponseEntity.ok(ApiResponse.ok("Job found", job));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Job>> createJob(@RequestBody Job job) {
        Job savedJob = jobRepository.save(job);
        return ResponseEntity.ok(ApiResponse.ok("Job created successfully", savedJob));
    }
}

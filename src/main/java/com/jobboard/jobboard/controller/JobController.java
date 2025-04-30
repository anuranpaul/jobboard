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
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String minSalary,
            Pageable pageable) {
        
        log.debug("Searching jobs - keyword: {}, location: {}, tag: {}, minSalary: {}, pageable: {}", 
            keyword, location, tag, minSalary, pageable);
            
        Page<Job> jobsPage = jobService.searchJobs(keyword, location, tag, minSalary, pageable);
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

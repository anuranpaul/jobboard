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

import java.util.List;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final JobRepository jobRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Job>>> getJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String minSalary) {
        List<Job> jobs = jobService.searchJobs(keyword, location, tag, minSalary);
        if (jobs.isEmpty()) {
            throw new NotFoundException(Messages.NO_JOBS_FOUND);
        }
        return ResponseEntity.ok(ApiResponse.ok(Messages.JOBS_FOUND, jobs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Job>> getJobById(@PathVariable Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(Messages.JOB_NOT_FOUND + id));

        return ResponseEntity.ok(ApiResponse.ok("Job found", job));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Job>> createJob(@RequestBody Job job) {
        Job savedJob = jobRepository.save(job);
        return ResponseEntity.ok(ApiResponse.ok("Job created successfully", savedJob));
    }

}

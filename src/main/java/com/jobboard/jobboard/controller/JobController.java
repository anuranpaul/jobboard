package com.jobboard.jobboard.controller;

import com.jobboard.jobboard.model.Job;
import com.jobboard.jobboard.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;  
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import com.jobboard.jobboard.dto.ApiResponse;
import com.jobboard.jobboard.exceptions.NotFoundException;
import com.jobboard.jobboard.constants.Messages;

import java.util.List;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobRepository jobRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Job>>> getAllJobs(
        @RequestParam(required = false) String location,
        @RequestParam(required = false) String tag
    ) {
        List<Job> jobs;

        if (location != null) {
            jobs = jobRepository.findByLocationContainingIgnoreCase(location);
        } else if (tag != null) {
            jobs = jobRepository.findByTagsContainingIgnoreCase(tag);
        } else {
            jobs = jobRepository.findAll();
        }

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
}


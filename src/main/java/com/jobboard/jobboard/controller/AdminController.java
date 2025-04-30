package com.jobboard.jobboard.controller;

import com.jobboard.jobboard.dto.ApiResponse;
import com.jobboard.jobboard.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final JobService jobService;

    @PostMapping("/cache/clear")
    public ResponseEntity<ApiResponse<String>> clearCache() {
        jobService.clearCache();
        return ResponseEntity.ok(ApiResponse.ok("Cache cleared successfully", null));
    }
} 
package com.jobboard.jobboard.controller;

import com.jobboard.jobboard.dto.ApiResponse;
import com.jobboard.jobboard.constants.Messages;
import com.jobboard.jobboard.service.JobScraperService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/scraper")
@RequiredArgsConstructor
public class ScraperController {

    private final JobScraperService scraperService;

    @PostMapping("/run")
    public ResponseEntity<ApiResponse<String>> runScraper() {
        try {
            int count = scraperService.scrapeAndSaveJobs();
            String message = count > 0 ? Messages.SCRAPE_SUCCESS + " Total: " + count : Messages.NO_JOBS_FOUND;
            return ResponseEntity.ok(ApiResponse.ok(message, null));
        } catch (Exception e) {
            throw new RuntimeException("Scraping failed: " + e.getMessage());
        }
    }
}

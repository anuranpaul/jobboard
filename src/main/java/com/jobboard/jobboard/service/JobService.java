package com.jobboard.jobboard.service;

import com.jobboard.jobboard.model.Job;
import com.jobboard.jobboard.repository.JobRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;
import com.jobboard.jobboard.util.SalaryParser;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;

    public Page<Job> searchJobs(String keyword, String location, String tag, String minSalary, Pageable pageable) {
        // Parse minSalary if provided
        Double minSalaryValue = null;
        if (minSalary != null && !minSalary.isEmpty()) {
            try {
                minSalaryValue = Double.parseDouble(minSalary);
            } catch (NumberFormatException e) {
                // Log invalid format but don't fail the search
                log.warn("Invalid minSalary format: {}", minSalary);
            }
        }
        
        return jobRepository.searchJobs(keyword, location, tag, minSalaryValue, pageable);
    }

    private String normalizeSearchTerm(String term) {
        if (term == null || term.trim().isEmpty()) {
            return null;
        }
        
        // Remove extra whitespace and convert to lowercase
        return term.trim().toLowerCase();
    }
}

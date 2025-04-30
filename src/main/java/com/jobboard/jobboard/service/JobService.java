package com.jobboard.jobboard.service;

import com.jobboard.jobboard.model.Job;
import com.jobboard.jobboard.repository.JobRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;
import com.jobboard.jobboard.util.SalaryParser;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import com.jobboard.jobboard.service.JobCacheService;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final JobCacheService jobCacheService;

    public Page<Job> searchJobs(String keyword, String location, String tag, String minSalary, Pageable pageable) {
        log.info("Processing search query");
        Double minSalaryValue = null;
        if (minSalary != null && !minSalary.isEmpty()) {
            try {
                minSalaryValue = Double.parseDouble(minSalary);
            } catch (NumberFormatException e) {
                log.warn("Invalid minSalary format: {}", minSalary);
            }
        }
        
        // Use the cache service instead of direct method call
        List<Job> jobs = jobCacheService.findJobs(keyword, location, tag, minSalaryValue, 
            pageable.getPageNumber(), pageable.getPageSize(), 
            pageable.getSort() != null ? pageable.getSort().toString() : null);
        
        // Use the cache service for counting
        long total = jobCacheService.countJobs(keyword, location, tag, minSalaryValue);
        return new PageImpl<>(jobs, pageable, total);
    }

    @Cacheable(value = "jobs", key = "#id")
    public Job getJobById(Long id) {
        log.info("Cache miss for job id: {} - fetching from database", id);
        return jobRepository.findById(id).orElse(null);
    }
    
    @CacheEvict(value = {"jobSearches", "jobs"}, allEntries = true)
    public void clearCache() {
        log.info("Clearing job caches after data update");
    }

    @Cacheable(value = "popularTags", key = "'top10'")
    public List<String> getPopularTags(int limit) {
        log.info("Fetching popular tags from database");
        return jobRepository.findTopTags(limit);
    }

    private String normalizeSearchTerm(String term) {
        if (term == null || term.trim().isEmpty()) {
            return null;
        }
        
        // Remove extra whitespace and convert to lowercase
        return term.trim().toLowerCase();
    }
}

package com.jobboard.jobboard.service;

import com.jobboard.jobboard.model.Job;
import com.jobboard.jobboard.repository.JobRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobCacheService {

    private final JobRepository jobRepository;

    @Cacheable(value = "jobSearches", key = "{#keyword, #location, #tag, #minSalary, #page, #size, #sort}")
    public List<Job> findJobs(String keyword, String location, String tag, Double minSalary, 
                            int page, int size, String sort) {
        log.info("Cache miss - fetching jobs from database");
        
        PageRequest pageable = createPageRequest(page, size, sort);
        return jobRepository.searchJobs(keyword, location, tag, minSalary, pageable).getContent();
    }

    @Cacheable(value = "jobCounts", key = "{#keyword, #location, #tag, #minSalary}")
    public long countJobs(String keyword, String location, String tag, Double minSalary) {
        log.info("Cache miss - counting jobs from database");
        return jobRepository.countJobs(keyword, location, tag, minSalary);
    }
    
    private PageRequest createPageRequest(int page, int size, String sort) {
        if (sort != null && !sort.isEmpty() && !sort.equals("UNSORTED")) {
            try {
                String[] parts = sort.split(",");
                String property = parts[0];
                Sort.Direction direction = parts.length > 1 ? 
                    Sort.Direction.fromString(parts[1]) : Sort.Direction.ASC;
                return PageRequest.of(page, size, direction, property);
            } catch (Exception e) {
                return PageRequest.of(page, size);
            }
        }
        return PageRequest.of(page, size);
    }
} 
package com.jobboard.jobboard.service;

import com.jobboard.jobboard.model.Job;
import com.jobboard.jobboard.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class JobService {

    @Autowired
    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {    
        this.jobRepository = jobRepository;
    }
    public List<Job> searchJobs(String keyword, String location, String tag, String minSalary) {
        return jobRepository.searchJobs(
                keyword != null ? "%" + keyword.toLowerCase() + "%" : null,
                location != null ? "%" + location.toLowerCase() + "%" : null,
                tag != null ? "%" + tag.toLowerCase() + "%" : null,
                minSalary != null ? minSalary : null);
    }
    
}

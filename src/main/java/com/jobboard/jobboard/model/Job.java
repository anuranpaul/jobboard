package com.jobboard.jobboard.model;

import lombok.Data;
import jakarta.persistence.*;

@Entity
@Data
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String companyName;
    private String location;
    private String url;
    private String description;
    private String salary;
    private String tags; // comma-separated tech stack
}

package com.jobboard.jobboard.model;

import lombok.Data;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "job")
@Data
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "company_name")
    private String companyName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private String location;

    @Column
    private String salary; // Keep original salary string for display

    @Column(name = "min_salary")
    private Double minSalary; // Normalized minimum salary

    @Column(name = "max_salary")
    private Double maxSalary; // Normalized maximum salary

    @Column(columnDefinition = "TEXT")
    private String tags;

    @Column(columnDefinition = "TEXT")
    private String url;

    /**
     * PostgreSQL tsvector field for full-text search.
     * Excluded from JSON serialization to reduce cache size.
     */
    @JsonIgnore
    @Column(name = "search_index",
            columnDefinition = "tsvector",
            insertable = false,
            updatable = false)
    private String searchIndex;
}

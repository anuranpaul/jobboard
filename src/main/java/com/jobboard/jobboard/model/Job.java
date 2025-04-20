package com.jobboard.jobboard.model;

import lombok.Data;
import jakarta.persistence.*;

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
    private String salary;

    @Column(columnDefinition = "TEXT")
    private String tags;

    @Column(columnDefinition = "TEXT")
    private String url;

    // Optional: just for debugging or testing
    @Column(name = "search_index", insertable = false, updatable = false)
    private String searchIndex;
}

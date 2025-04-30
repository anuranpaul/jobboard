package com.jobboard.jobboard.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JobSearchRequest {
    @Size(max = 100, message = "Keyword must be less than 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-_.,]+$", message = "Keyword contains invalid characters")
    private String keyword;
    
    @Size(max = 100, message = "Location must be less than 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-_.,]+$", message = "Location contains invalid characters")
    private String location;
    
    @Size(max = 50, message = "Tag must be less than 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-_.,]+$", message = "Tag contains invalid characters")
    private String tag;
    
    @Pattern(regexp = "^\\d+(\\.\\d+)?$", message = "Min salary must be a valid number")
    private String minSalary;
} 
package com.jobboard.jobboard.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemotiveJobDTO {
    private String title;
    private String company_name;
    private String candidate_required_location;
    private String url;
    private String description;
    private String salary;
    private List<String> tags;
}

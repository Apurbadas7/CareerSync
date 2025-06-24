package com.AD.ResumeMatcher.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobDTO {
    private Long id;
    private String title;
    private String company;
    private String description;
    private List<String> requiredSkills;

    private double matchPercentage;
    private String fitLevel;
}

package com.AD.ResumeMatcher.Entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)

public class Resume {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private long id;
    private String name;
    private int experience;

    private String resumeFilePath;
    private String originalFileName;

    @Lob
    private String resumeText;

    @ElementCollection
    private List<String> skills;
    public Resume(String name, int experience, String resumeFilePath, String originalFileName, String resumeText, List<String> skills) {
        this.name = name;
        this.experience = experience;
        this.resumeFilePath = resumeFilePath;
        this.originalFileName = originalFileName;
        this.resumeText = resumeText;
        this.skills = skills;
    }

}

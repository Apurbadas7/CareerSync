package com.AD.ResumeMatcher.Service;

import com.AD.ResumeMatcher.DTO.JobDTO;
import com.AD.ResumeMatcher.Entity.Job;
import com.AD.ResumeMatcher.Entity.Resume;
import com.AD.ResumeMatcher.Repository.JobRepository;
import com.AD.ResumeMatcher.Repository.ResumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepo;

    @Autowired
    private ResumeRepository resumeRepo;

    public List<JobDTO> matchJobsForResume(Long resumeId) {
        Resume resume = resumeRepo.findById(resumeId).orElseThrow();
        List<String> resumeSkills = resume.getSkills().stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        List<JobDTO> matchedJobs = new ArrayList<>();

        for (Job job : jobRepo.findAll()) {
            List<String> jobSkills = job.getRequiredSkills().stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());

            if (hasOverlap(resumeSkills, jobSkills)) {
                double matchPercentage = calculatePercentage(resumeSkills, jobSkills);
                String fitLevel = fit(matchPercentage);

                JobDTO dto = new JobDTO(
                        job.getId(),
                        job.getTitle(),
                        job.getCompany(),
                        job.getDescription(),
                        job.getRequiredSkills(),
                        matchPercentage,
                        fitLevel
                );

                matchedJobs.add(dto);
            }
        }

        return matchedJobs;
    }

    private boolean hasOverlap(List<String> resumeSkills, List<String> jobSkills) {
        for (String resumeSkill : resumeSkills) {
            for (String jobSkill : jobSkills) {
                if (jobSkill.contains(resumeSkill) || resumeSkill.contains(jobSkill)) {
                    return true;
                }
            }
        }
        return false;
    }

    public double calculatePercentage(List<String> resumeSkills, List<String> jobSkills) {
        if (resumeSkills == null || jobSkills == null || jobSkills.isEmpty()) {
            return 0.0;
        }

        long matchedCount = jobSkills.stream()
                .filter(jobSkill -> resumeSkills.stream()
                        .anyMatch(resumeSkill -> jobSkill.contains(resumeSkill) || resumeSkill.contains(jobSkill)))
                .count();

        return (matchedCount * 100.0) / jobSkills.size();
    }

    private String fit(double percentage) {
        if (percentage >= 75.0) return "High";
        else if (percentage >= 40.0) return "Medium";
        else return "Low";
    }

    public Job addJob(Job job) {
        return jobRepo.save(job);
    }
}

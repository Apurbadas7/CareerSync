package com.AD.ResumeMatcher.Controller;


import com.AD.ResumeMatcher.DTO.JobDTO;
import com.AD.ResumeMatcher.Entity.Job;
import com.AD.ResumeMatcher.Entity.Resume;
import com.AD.ResumeMatcher.Service.JobService;
import com.AD.ResumeMatcher.Service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin("*")
    @RequestMapping("/resumes")
    public class JobController {

        @Autowired
        private ResumeService resumeService;
        @Autowired private JobService jobService;

        @PostMapping("/upload")
        public ResponseEntity<Resume> uploadResume(@RequestParam MultipartFile file) throws Exception {
            Resume resume = resumeService.uploadAndExtract(file);
            return ResponseEntity.ok(resume);
        }

        @GetMapping("/{id}/match-jobs")
        public ResponseEntity<List<JobDTO>> getMatchingJobs(@PathVariable Long id) {
            List<JobDTO> matchedJobs = jobService.matchJobsForResume(id);
            return ResponseEntity.ok(matchedJobs);
        }

        @PostMapping("/addJob")
    public Job addJob(@RequestBody Job job){
            return jobService.addJob(job);


        }

    }



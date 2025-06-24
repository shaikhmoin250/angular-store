package com.example.jobportal.controller;

import com.example.jobportal.model.JobListing;
import com.example.jobportal.service.JobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<JobListing> create(@RequestBody JobListing job) {
        return ResponseEntity.ok(jobService.save(job));
    }

    @GetMapping
    public List<JobListing> list() { return jobService.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<JobListing> get(@PathVariable Long id) {
        return jobService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        jobService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

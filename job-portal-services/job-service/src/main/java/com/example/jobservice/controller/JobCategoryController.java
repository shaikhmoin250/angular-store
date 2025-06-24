package com.example.jobservice.controller;

import com.example.jobservice.model.JobCategory;
import com.example.jobservice.service.JobCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class JobCategoryController {
    private final JobCategoryService jobCategoryService;

    public JobCategoryController(JobCategoryService jobCategoryService) {
        this.jobCategoryService = jobCategoryService;
    }

    @PostMapping
    public ResponseEntity<JobCategory> create(@RequestBody JobCategory category) {
        return ResponseEntity.ok(jobCategoryService.save(category));
    }

    @GetMapping
    public List<JobCategory> list() { return jobCategoryService.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<JobCategory> get(@PathVariable Long id) {
        return jobCategoryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        jobCategoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

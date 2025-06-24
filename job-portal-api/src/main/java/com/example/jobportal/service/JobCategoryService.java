package com.example.jobportal.service;

import com.example.jobportal.model.JobCategory;
import com.example.jobportal.repository.JobCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JobCategoryService {
    private final JobCategoryRepository jobCategoryRepository;

    public JobCategoryService(JobCategoryRepository repo) {
        this.jobCategoryRepository = repo;
    }

    public JobCategory save(JobCategory category) { return jobCategoryRepository.save(category); }

    public List<JobCategory> findAll() { return jobCategoryRepository.findAll(); }

    public Optional<JobCategory> findById(Long id) { return jobCategoryRepository.findById(id); }

    public void deleteById(Long id) { jobCategoryRepository.deleteById(id); }
}

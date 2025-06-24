package com.example.jobportal.service;

import com.example.jobportal.model.JobListing;
import com.example.jobportal.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JobService {
    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public JobListing save(JobListing job) { return jobRepository.save(job); }

    public List<JobListing> findAll() { return jobRepository.findAll(); }

    public Optional<JobListing> findById(Long id) { return jobRepository.findById(id); }

    public void deleteById(Long id) { jobRepository.deleteById(id); }
}

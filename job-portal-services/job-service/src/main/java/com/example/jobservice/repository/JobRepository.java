package com.example.jobservice.repository;

import com.example.jobservice.model.JobListing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<JobListing, Long> {
}

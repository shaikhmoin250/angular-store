package com.example.jobportal.repository;

import com.example.jobportal.model.JobListing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<JobListing, Long> {
}

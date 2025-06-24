package com.example.jobportal.repository;

import com.example.jobportal.model.JobCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobCategoryRepository extends JpaRepository<JobCategory, Long> {
}

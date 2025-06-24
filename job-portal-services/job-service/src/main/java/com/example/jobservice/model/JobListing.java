package com.example.jobservice.model;

import jakarta.persistence.*;

@Entity
public class JobListing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    @ManyToOne
    private JobCategory category;

    // getters and setters
    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}
    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}
    public JobCategory getCategory() {return category;}
    public void setCategory(JobCategory category) {this.category = category;}
}

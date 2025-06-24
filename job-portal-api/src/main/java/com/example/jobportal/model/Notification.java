package com.example.jobportal.model;

import jakarta.persistence.*;

@Entity
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String message;
    @ManyToOne
    private User user;
    private boolean read;

    // getters and setters
    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public String getMessage() {return message;}
    public void setMessage(String message) {this.message = message;}
    public User getUser() {return user;}
    public void setUser(User user) {this.user = user;}
    public boolean isRead() {return read;}
    public void setRead(boolean read) {this.read = read;}
}

package com.example.jobportal.service;

import com.example.jobportal.model.Notification;
import com.example.jobportal.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository repo) {
        this.notificationRepository = repo;
    }

    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

    public List<Notification> unreadForUser(Long userId) {
        return notificationRepository.findByUserIdAndReadFalse(userId);
    }
}

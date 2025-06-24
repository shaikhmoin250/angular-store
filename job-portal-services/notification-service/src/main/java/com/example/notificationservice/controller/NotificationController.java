package com.example.notificationservice.controller;

import com.example.notificationservice.model.Notification;
import com.example.notificationservice.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public Notification create(@RequestBody Notification notification) {
        return notificationService.save(notification);
    }

    @GetMapping("/unread/{userId}")
    public List<Notification> unread(@PathVariable Long userId) {
        return notificationService.unreadForUser(userId);
    }
}

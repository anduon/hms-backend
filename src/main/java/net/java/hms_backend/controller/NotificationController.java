package net.java.hms_backend.controller;

import net.java.hms_backend.dto.NotificationDto;
import net.java.hms_backend.entity.User;
import net.java.hms_backend.exception.ResourceNotFoundException;
import net.java.hms_backend.repository.UserRepository;
import net.java.hms_backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getNotificationsForCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<NotificationDto> notifications = notificationService.getNotifications(username);
        return ResponseEntity.ok(notifications);
    }


    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        long count = notificationService.countUnread(username);
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/test-receptionists")
    public ResponseEntity<List<String>> testReceptionists() {
        List<User> receptionists = userRepository.findByRoles_Name("RECEPTIONIST");

        List<String> info = receptionists.stream()
                .map(user -> user.getFullName() + " - " + user.getEmail())
                .collect(Collectors.toList());

        return ResponseEntity.ok(info);
    }

}

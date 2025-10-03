package net.java.hms_backend.service;

import net.java.hms_backend.dto.NotificationDto;
import net.java.hms_backend.entity.User;

import java.util.List;
import java.util.Optional;

public interface NotificationService {

    void sendNotification(User recipient, String type, String title, String message);

    List<NotificationDto> getNotifications(String email);

    long countUnread(String email);

    void markAsRead(Long notificationId);

    void deleteNotification(Long notificationId);

    void notifyAdminsAndManagers(String type, String title, String message);

}

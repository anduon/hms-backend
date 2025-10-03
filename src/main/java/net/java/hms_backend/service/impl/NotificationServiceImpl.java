package net.java.hms_backend.service.impl;

import net.java.hms_backend.dto.NotificationDto;
import net.java.hms_backend.entity.Notification;
import net.java.hms_backend.entity.User;
import net.java.hms_backend.exception.ResourceNotFoundException;
import net.java.hms_backend.mapper.NotificationMapper;
import net.java.hms_backend.repository.NotificationRepository;
import net.java.hms_backend.repository.UserRepository;
import net.java.hms_backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public void sendNotification(User recipient, String type, String title, String message) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Override
    public List<NotificationDto> getNotifications(String email) {
        List<Notification> notifications = notificationRepository
                .findByRecipientEmailOrderByCreatedAtDesc(email);
        return notificationMapper.toDTOList(notifications);
    }

    @Override
    public long countUnread(String email) {
        return notificationRepository.countByRecipientEmailAndReadFalse(email);
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void deleteNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
        notificationRepository.delete(notification);
    }

    public void notifyAdminsAndManagers(String type, String title, String message) {
        List<User> recipients = userRepository.findByRoles_NameIn(List.of("ADMIN", "MANAGER"));
        for (User user : recipients) {
            sendNotification(user, type, title, message);
        }
    }

    public void notifyReceptionists(String type, String title, String message) {
        List<User> recipients = userRepository.findByRoles_Name("RECEPTIONIST");
        for (User user : recipients) {
            sendNotification(user, type, title, message);
        }
    }
}


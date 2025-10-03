package net.java.hms_backend.mapper;

import net.java.hms_backend.dto.NotificationDto;
import net.java.hms_backend.entity.Notification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NotificationMapper {

    public NotificationDto toDTO(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setType(notification.getType());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }

    public List<NotificationDto> toDTOList(List<Notification> notifications) {
        return notifications.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}

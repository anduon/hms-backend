package net.java.hms_backend.service;

import net.java.hms_backend.entity.ActivityLog;
import net.java.hms_backend.repository.ActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditLogService {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    public void log(String username, String action, String entity, Long entityId, String details) {
        ActivityLog log = new ActivityLog();
        log.setTimestamp(LocalDateTime.now());
        log.setUsername(username);
        log.setAction(action);
        log.setEntity(entity);
        log.setEntityId(entityId);
        log.setDetails(details);
        activityLogRepository.save(log);
    }
}


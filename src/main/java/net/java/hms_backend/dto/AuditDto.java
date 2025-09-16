package net.java.hms_backend.dto;

import java.time.LocalDateTime;

public interface AuditDto {
    void setCreatedBy(String createdBy);
    void setUpdatedBy(String updatedBy);
    void setCreatedAt(LocalDateTime createdAt);
    void setUpdatedAt(LocalDateTime updatedAt);
}

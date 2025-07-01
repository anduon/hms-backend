package net.java.hms_backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RoomFilterRequest {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer numberOfGuests;
}
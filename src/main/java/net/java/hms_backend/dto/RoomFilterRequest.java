package net.java.hms_backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RoomFilterRequest {
    private String roomType;
    private String status;
    private String location;
    private Integer maxOccupancy;
    private LocalDateTime desiredCheckIn;
    private LocalDateTime desiredCheckOut;
}
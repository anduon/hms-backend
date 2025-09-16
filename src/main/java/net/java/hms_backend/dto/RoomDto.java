package net.java.hms_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomDto implements AuditDto {
    private Long id;
    @NotNull(message = "Room number is required")
    private Integer roomNumber;
    private Integer maxOccupancy;
    private String roomType;
    private String status;
    private String location;

    private List<RoomPriceDto> prices;

    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

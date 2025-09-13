package net.java.hms_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomDto {
    private Long id;
    @NotNull(message = "Room number is required")
    private Integer roomNumber;
    private Integer maxOccupancy;
    private String roomType;
    private String status;
    private String location;

    private List<RoomPriceDto> prices;
}

package net.java.hms_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomDto {
    private Long id;
    private Integer roomNumber;
    private Integer maxOccupancy;
    private String roomType;
    private Double price;
    private String status;
    private String location;
}

package net.java.hms_backend.dto;

import lombok.Data;
import java.time.LocalTime;

@Data
public class HotelInfoDto {
    private String name;
    private String address;
    private String phone;
    private String email;
    private String taxCode;
    private Integer numberOfFloors;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
}

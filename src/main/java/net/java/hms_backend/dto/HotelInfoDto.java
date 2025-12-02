package net.java.hms_backend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class HotelInfoDto implements AuditDto {
    private String name;
    private String address;
    private String phone;
    private String email;
    private String taxCode;
    private Integer numberOfFloors;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private Double weekendSurchargePercent;

    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package net.java.hms_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.java.hms_backend.entity.base.Auditable;

import java.time.LocalTime;

@Entity
@Table(name = "hotel_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelInfo extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;
    private String phone;
    private String email;
    private String taxCode;
    private Integer numberOfFloors;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private Double weekendSurchargePercent;
}


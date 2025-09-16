package net.java.hms_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.java.hms_backend.entity.base.Auditable;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "booking")
public class Booking extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String guestFullName;
    private String guestIdNumber;
    private String guestNationality;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private LocalDateTime actualCheckInTime;
    private LocalDateTime actualCheckOutTime;


    private String bookingType;
    private String status;
    private int numberOfGuests;
    private String notes;
    private String cancelReason;

}


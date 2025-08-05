package net.java.hms_backend.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {
    private Long id;
    private String guestFullName;
    private String guestIdNumber;
    private String guestNationality;
    private Long roomId;

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

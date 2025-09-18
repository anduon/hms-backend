package net.java.hms_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto implements AuditDto {
    private Long id;

    @NotBlank(message = "Guest name is required")
    private String guestFullName;

    @NotBlank(message = "ID number is required")
    private String guestIdNumber;
    private String guestNationality;

    @NotNull(message = "Room number is required")
    private Integer roomNumber;

    private Long roomId;

    @NotNull(message = "Check-in date is required")
    private LocalDateTime checkInDate;

    @NotNull(message = "Check-out date is required")
    private LocalDateTime checkOutDate;
    private LocalDateTime actualCheckInTime;
    private LocalDateTime actualCheckOutTime;

    @NotBlank(message = "Booking type is required")
    private String bookingType;

    @NotBlank(message = "Status is required")
    private String status;

    @NotNull(message = "Number of guests is required")
    private Integer numberOfGuests;
    private String notes;
    private String cancelReason;

    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

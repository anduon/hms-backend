package net.java.hms_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BookingFilterRequest {

    private String guestFullName;
    private String guestIdNumber;
    private String guestNationality;

    private Long roomId;

    private LocalDateTime checkInDateFrom;
    private LocalDateTime checkInDateTo;

    private LocalDateTime checkOutDateFrom;
    private LocalDateTime checkOutDateTo;

    private String bookingType;
    private String status;

    private Integer numberOfGuestsMin;
    private Integer numberOfGuestsMax;
}

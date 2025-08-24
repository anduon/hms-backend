package net.java.hms_backend.mapper;

import net.java.hms_backend.dto.BookingDto;
import net.java.hms_backend.entity.Booking;
import net.java.hms_backend.entity.Room;

public class BookingMapper {

    public static BookingDto toDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getGuestFullName(),
                booking.getGuestIdNumber(),
                booking.getGuestNationality(),
                booking.getRoom() != null ? booking.getRoom().getId() : null,
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getActualCheckInTime(),
                booking.getActualCheckOutTime(),
                booking.getBookingType(),
                booking.getStatus(),
                booking.getNumberOfGuests(),
                booking.getNotes(),
                booking.getCancelReason()
        );
    }

    public static Booking toEntity(BookingDto dto, Room room) {
        Booking booking = new Booking();
        booking.setId(dto.getId());
        booking.setGuestFullName(dto.getGuestFullName());
        booking.setGuestIdNumber(dto.getGuestIdNumber());
        booking.setGuestNationality(dto.getGuestNationality());
        booking.setRoom(room);
        booking.setCheckInDate(dto.getCheckInDate());
        booking.setCheckOutDate(dto.getCheckOutDate());
        booking.setActualCheckInTime(dto.getActualCheckInTime());
        booking.setActualCheckOutTime(dto.getActualCheckOutTime());
        booking.setBookingType(dto.getBookingType());
        booking.setStatus(dto.getStatus());
        booking.setNumberOfGuests(dto.getNumberOfGuests());
        booking.setNotes(dto.getNotes());
        booking.setCancelReason(dto.getCancelReason());
        return booking;
    }
}

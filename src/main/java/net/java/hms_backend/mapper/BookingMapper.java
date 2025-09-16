package net.java.hms_backend.mapper;

import net.java.hms_backend.dto.BookingDto;
import net.java.hms_backend.entity.Booking;
import net.java.hms_backend.entity.Room;

public class BookingMapper {

    public static BookingDto toDto(Booking booking) {
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setGuestFullName(booking.getGuestFullName());
        dto.setGuestIdNumber(booking.getGuestIdNumber());
        dto.setGuestNationality(booking.getGuestNationality());
        dto.setRoomNumber(booking.getRoom() != null ? booking.getRoom().getRoomNumber() : null);
        dto.setCheckInDate(booking.getCheckInDate());
        dto.setCheckOutDate(booking.getCheckOutDate());
        dto.setActualCheckInTime(booking.getActualCheckInTime());
        dto.setActualCheckOutTime(booking.getActualCheckOutTime());
        dto.setBookingType(booking.getBookingType());
        dto.setStatus(booking.getStatus());
        dto.setNumberOfGuests(booking.getNumberOfGuests());
        dto.setNotes(booking.getNotes());
        dto.setCancelReason(booking.getCancelReason());

        BaseMapper.mapAuditFields(booking, dto);

        return dto;
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

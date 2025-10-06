package net.java.hms_backend.service;

import net.java.hms_backend.dto.BookingExtraChargeDto;

import java.util.List;

public interface BookingExtraChargeService {
    BookingExtraChargeDto create(BookingExtraChargeDto dto);
    BookingExtraChargeDto update(Long id, BookingExtraChargeDto dto);
    void delete(Long id);
    BookingExtraChargeDto getById(Long id);
    List<BookingExtraChargeDto> getAll();
    List<BookingExtraChargeDto> getByBookingId(Long bookingId);
}

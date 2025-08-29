package net.java.hms_backend.service;

import net.java.hms_backend.dto.BookingDto;
import net.java.hms_backend.dto.BookingFilterRequest;
import org.springframework.data.domain.Page;

public interface BookingService {
    BookingDto createBooking(BookingDto bookingDto);
    BookingDto getBookingById(Long id);
    Page<BookingDto> getAllBookings(int page, int size);
    BookingDto updateBooking(Long id, BookingDto bookingDto);
    void deleteBooking(Long id);
    Page<BookingDto> filterBookings(BookingFilterRequest request, int page, int size);
}

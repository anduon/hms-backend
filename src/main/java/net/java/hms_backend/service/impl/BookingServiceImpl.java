package net.java.hms_backend.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import net.java.hms_backend.dto.BookingDto;
import net.java.hms_backend.entity.Booking;
import net.java.hms_backend.entity.Room;
import net.java.hms_backend.exception.ResourceNotFoundException;
import net.java.hms_backend.mapper.BookingMapper;
import net.java.hms_backend.repository.BookingRepository;
import net.java.hms_backend.repository.RoomRepository;
import net.java.hms_backend.service.BookingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    @Override
    public BookingDto createBooking(BookingDto dto) {
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", dto.getRoomId()));

        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                dto.getRoomId(),
                dto.getCheckInDate(),
                dto.getCheckOutDate()
        );

        if (!overlappingBookings.isEmpty()) {
            throw new IllegalArgumentException("The selected room is already booked during the requested period.");
        }

        Booking booking = BookingMapper.toEntity(dto, room);
        return BookingMapper.toDto(bookingRepository.save(booking));
    }


    @Override
    public BookingDto getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
        return BookingMapper.toDto(booking);
    }

    @Override
    public Page<BookingDto> getAllBookings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> bookingsPage = bookingRepository.findAll(pageable);
        return bookingsPage.map(BookingMapper::toDto);
    }

    @Override
    public BookingDto updateBooking(Long id, BookingDto dto) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", dto.getRoomId()));

        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                        dto.getRoomId(),
                        dto.getCheckInDate(),
                        dto.getCheckOutDate()
                ).stream()
                .filter(b -> !b.getId().equals(id))
                .toList();

        if (!overlappingBookings.isEmpty()) {
            throw new IllegalArgumentException("The selected room is already booked during the requested period.");
        }

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

        return BookingMapper.toDto(bookingRepository.save(booking));
    }


    @Override
    public void deleteBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
        bookingRepository.delete(booking);
    }

}

package net.java.hms_backend.service.impl;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import net.java.hms_backend.dto.BookingDto;
import net.java.hms_backend.dto.BookingFilterRequest;
import net.java.hms_backend.entity.Booking;
import net.java.hms_backend.entity.Room;
import net.java.hms_backend.exception.BookingException;
import net.java.hms_backend.exception.ResourceNotFoundException;
import net.java.hms_backend.mapper.BookingMapper;
import net.java.hms_backend.repository.BookingRepository;
import net.java.hms_backend.repository.RoomRepository;
import net.java.hms_backend.service.BookingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public BookingDto createBooking(BookingDto dto) {
        if (dto.getGuestFullName() == null || dto.getGuestFullName().isBlank()) {
            throw new BookingException.MissingGuestNameException();
        }

        if (dto.getGuestIdNumber() == null || dto.getGuestIdNumber().isBlank()) {
            throw new BookingException.MissingIdNumberException();
        }

        if (dto.getRoomNumber() == null) {
            throw new BookingException.MissingRoomNumberException();
        }

        if (dto.getCheckInDate() == null) {
            throw new BookingException.MissingCheckInDateException();
        }

        if (dto.getCheckOutDate() == null) {
            throw new BookingException.MissingCheckOutDateException();
        }

        if (dto.getBookingType() == null || dto.getBookingType().isBlank()) {
            throw new BookingException.MissingBookingTypeException();
        }

        if (dto.getStatus() == null || dto.getStatus().isBlank()) {
            throw new BookingException.MissingStatusException();
        }

        if (dto.getNumberOfGuests() == null) {
            throw new BookingException.MissingNumberOfGuestsException();
        }

        Room room = roomRepository.findByRoomNumber(dto.getRoomNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Room", "roomNumber", dto.getRoomNumber()));

        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                room.getId(),
                dto.getCheckInDate(),
                dto.getCheckOutDate()
        );

        if (!overlappingBookings.isEmpty()) {
            throw new BookingException.BookingConflictException("Room is already booked during the requested period.");
        }

        if (dto.getCheckOutDate().isBefore(dto.getCheckInDate())) {
            throw new BookingException.InvalidDateRangeException();
        }

        Booking booking = BookingMapper.toEntity(dto, room);
        Booking saved = bookingRepository.save(booking);
        return BookingMapper.toDto(saved);
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

        if (dto.getRoomNumber() != null && !dto.getRoomNumber().equals(booking.getRoom().getRoomNumber())) {
            Room room = roomRepository.findByRoomNumber(dto.getRoomNumber())
                    .orElseThrow(() -> new ResourceNotFoundException("Room", "roomNumber", dto.getRoomNumber()));
            booking.setRoom(room);
        }

        if (dto.getCheckInDate() != null && dto.getCheckOutDate() != null) {
            if (dto.getCheckOutDate().isBefore(dto.getCheckInDate())) {
                throw new BookingException.InvalidDateRangeException();
            }

            Long roomId = booking.getRoom().getId();
            List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                            roomId,
                            dto.getCheckInDate(),
                            dto.getCheckOutDate()
                    ).stream()
                    .filter(b -> !b.getId().equals(id))
                    .toList();

            if (!overlappingBookings.isEmpty()) {
                throw new BookingException.BookingConflictException("Room is already booked during the requested period.");
            }

            booking.setCheckInDate(dto.getCheckInDate());
            booking.setCheckOutDate(dto.getCheckOutDate());
        }

        if (dto.getGuestFullName() != null && dto.getGuestFullName().isBlank()) {
            throw new BookingException.MissingGuestNameException();
        }
        if (dto.getGuestIdNumber() != null && dto.getGuestIdNumber().isBlank()) {
            throw new BookingException.MissingIdNumberException();
        }
        if (dto.getBookingType() != null && dto.getBookingType().isBlank()) {
            throw new BookingException.MissingBookingTypeException();
        }
        if (dto.getStatus() != null && dto.getStatus().isBlank()) {
            throw new BookingException.MissingStatusException();
        }

        if (dto.getGuestFullName() != null) booking.setGuestFullName(dto.getGuestFullName());
        if (dto.getGuestIdNumber() != null) booking.setGuestIdNumber(dto.getGuestIdNumber());
        if (dto.getGuestNationality() != null) booking.setGuestNationality(dto.getGuestNationality());
        if (dto.getActualCheckInTime() != null) booking.setActualCheckInTime(dto.getActualCheckInTime());
        if (dto.getActualCheckOutTime() != null) booking.setActualCheckOutTime(dto.getActualCheckOutTime());
        if (dto.getBookingType() != null) booking.setBookingType(dto.getBookingType());
        if (dto.getStatus() != null) booking.setStatus(dto.getStatus());
        if (dto.getNumberOfGuests() != null) booking.setNumberOfGuests(dto.getNumberOfGuests());
        if (dto.getNotes() != null) booking.setNotes(dto.getNotes());
        if (dto.getCancelReason() != null) booking.setCancelReason(dto.getCancelReason());

        Booking updatedBooking = bookingRepository.save(booking);
        return BookingMapper.toDto(updatedBooking);
    }



    @Override
    public void deleteBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
        bookingRepository.delete(booking);
    }

    @Override
    public Page<BookingDto> filterBookings(BookingFilterRequest filter, int page, int size) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Booking> query = cb.createQuery(Booking.class);
        Root<Booking> booking = query.from(Booking.class);
        query.select(booking).distinct(true);

        List<Predicate> predicates = buildPredicates(cb, booking, filter);
        query.where(cb.and(predicates.toArray(new Predicate[0])));

        Pageable pageable = PageRequest.of(page, size);

        List<Booking> result = entityManager.createQuery(query)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Booking> countRoot = countQuery.from(Booking.class);
        List<Predicate> countPredicates = buildPredicates(cb, countRoot, filter);

        countQuery.select(cb.countDistinct(countRoot));
        countQuery.where(cb.and(countPredicates.toArray(new Predicate[0])));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        Page<Booking> bookingsPage = new PageImpl<>(result, pageable, total);
        return bookingsPage.map(BookingMapper::toDto);
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Booking> root, BookingFilterRequest filter) {
        List<Predicate> predicates = new ArrayList<>();

        if (filter.getGuestFullName() != null) {
            predicates.add(cb.like(cb.lower(root.get("guestFullName")), "%" + filter.getGuestFullName().toLowerCase() + "%"));
        }
        if (filter.getGuestIdNumber() != null) {
            predicates.add(cb.equal(root.get("guestIdNumber"), filter.getGuestIdNumber()));
        }
        if (filter.getGuestNationality() != null) {
            predicates.add(cb.equal(root.get("guestNationality"), filter.getGuestNationality()));
        }
        if (filter.getStatus() != null) {
            predicates.add(cb.equal(root.get("status"), filter.getStatus()));
        }
        if (filter.getBookingType() != null) {
            predicates.add(cb.equal(root.get("bookingType"), filter.getBookingType()));
        }
        if (filter.getCheckInDateFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("checkInDate"), filter.getCheckInDateFrom()));
        }
        if (filter.getCheckInDateTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("checkInDate"), filter.getCheckInDateTo()));
        }
        if (filter.getCheckOutDateFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("checkOutDate"), filter.getCheckOutDateFrom()));
        }
        if (filter.getCheckOutDateTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("checkOutDate"), filter.getCheckOutDateTo()));
        }
        if (filter.getRoomId() != null) {
            predicates.add(cb.equal(root.get("room").get("id"), filter.getRoomId()));
        }

        return predicates;
    }



}

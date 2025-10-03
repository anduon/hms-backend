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
import net.java.hms_backend.entity.PriceType;
import net.java.hms_backend.entity.Room;
import net.java.hms_backend.entity.User;
import net.java.hms_backend.exception.BookingException;
import net.java.hms_backend.exception.ResourceNotFoundException;
import net.java.hms_backend.mapper.BookingMapper;
import net.java.hms_backend.repository.BookingRepository;
import net.java.hms_backend.repository.InvoiceRepository;
import net.java.hms_backend.repository.RoomRepository;
import net.java.hms_backend.repository.UserRepository;
import net.java.hms_backend.service.AuditLogService;
import net.java.hms_backend.service.BookingService;
import net.java.hms_backend.service.NotificationService;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final AuditLogService auditLogService;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

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
        try {
            PriceType.valueOf(dto.getBookingType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BookingException.InvalidBookingTypeException("Invalid bookingType: " + dto.getBookingType());
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

        if (dto.getNumberOfGuests() > room.getMaxOccupancy()) {
            throw new BookingException.ExceedsRoomCapacityException(
                    "Number of guests (" + dto.getNumberOfGuests() + ") exceeds room capacity (" + room.getMaxOccupancy() + ")"
            );
        }

        if (!overlappingBookings.isEmpty()) {
            throw new BookingException.BookingConflictException("Room is already booked during the requested period.");
        }

        if (dto.getCheckOutDate().isBefore(dto.getCheckInDate())) {
            throw new BookingException.InvalidDateRangeException();
        }

        Booking booking = BookingMapper.toEntity(dto, room);
        Booking saved = bookingRepository.save(booking);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        String details = "Created booking for guest: " + dto.getGuestFullName() +
                ", ID number: " + dto.getGuestIdNumber() +
                ", nationality: " + dto.getGuestNationality() +
                ", roomNumber: " + dto.getRoomNumber() +
                ", checkIn: " + dto.getCheckInDate() +
                ", checkOut: " + dto.getCheckOutDate() +
                ", bookingType: " + dto.getBookingType() +
                ", status: " + dto.getStatus() +
                ", numberOfGuests: " + dto.getNumberOfGuests() +
                (dto.getNotes() != null ? ", notes: " + dto.getNotes() : "");

        auditLogService.log(
                username,
                "CREATE",
                "Booking",
                saved.getId(),
                details
        );

        return BookingMapper.toDto(saved);
    }



    @Override
    public BookingDto getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        String details = "Viewed booking with ID: " + id +
                ", guest: " + booking.getGuestFullName() +
                ", roomNumber: " + (booking.getRoom() != null ? booking.getRoom().getRoomNumber() : "null") +
                ", checkIn: " + booking.getCheckInDate() +
                ", checkOut: " + booking.getCheckOutDate();

        auditLogService.log(
                username,
                "READ",
                "Booking",
                id,
                details
        );
        return BookingMapper.toDto(booking);
    }

    @Override
    public Page<BookingDto> getAllBookings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Booking> bookingsPage = bookingRepository.findAll(pageable);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        String details = "Viewed booking list - page: " + page +
                ", size: " + size +
                ", total bookings: " + bookingsPage.getTotalElements();

        auditLogService.log(
                username,
                "READ",
                "Booking",
                null,
                details
        );
        return bookingsPage.map(BookingMapper::toDto);
    }

    @Override
    public BookingDto updateBooking(Long id, BookingDto dto) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        StringBuilder changes = new StringBuilder("Updated booking ID: " + id + ". Changes: ");

        if (dto.getRoomNumber() != null && !dto.getRoomNumber().equals(booking.getRoom().getRoomNumber())) {
            Room room = roomRepository.findByRoomNumber(dto.getRoomNumber())
                    .orElseThrow(() -> new ResourceNotFoundException("Room", "roomNumber", dto.getRoomNumber()));
            changes.append("roomNumber: ").append(booking.getRoom().getRoomNumber())
                    .append(" → ").append(dto.getRoomNumber()).append("; ");
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

            if (!dto.getCheckInDate().equals(booking.getCheckInDate())) {
                changes.append("checkInDate: ").append(booking.getCheckInDate())
                        .append(" → ").append(dto.getCheckInDate()).append("; ");
                booking.setCheckInDate(dto.getCheckInDate());
            }

            if (!dto.getCheckOutDate().equals(booking.getCheckOutDate())) {
                changes.append("checkOutDate: ").append(booking.getCheckOutDate())
                        .append(" → ").append(dto.getCheckOutDate()).append("; ");
                booking.setCheckOutDate(dto.getCheckOutDate());
            }
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

        if (dto.getGuestFullName() != null && !dto.getGuestFullName().equals(booking.getGuestFullName())) {
            changes.append("guestFullName: ").append(booking.getGuestFullName())
                    .append(" → ").append(dto.getGuestFullName()).append("; ");
            booking.setGuestFullName(dto.getGuestFullName());
        }

        if (dto.getGuestIdNumber() != null && !dto.getGuestIdNumber().equals(booking.getGuestIdNumber())) {
            changes.append("guestIdNumber: ").append(booking.getGuestIdNumber())
                    .append(" → ").append(dto.getGuestIdNumber()).append("; ");
            booking.setGuestIdNumber(dto.getGuestIdNumber());
        }

        if (dto.getGuestNationality() != null && !dto.getGuestNationality().equals(booking.getGuestNationality())) {
            changes.append("guestNationality: ").append(booking.getGuestNationality())
                    .append(" → ").append(dto.getGuestNationality()).append("; ");
            booking.setGuestNationality(dto.getGuestNationality());
        }

        if (dto.getActualCheckInTime() != null && !dto.getActualCheckInTime().equals(booking.getActualCheckInTime())) {
            changes.append("actualCheckInTime: ").append(booking.getActualCheckInTime())
                    .append(" → ").append(dto.getActualCheckInTime()).append("; ");
            booking.setActualCheckInTime(dto.getActualCheckInTime());
        }

        if (dto.getActualCheckOutTime() != null && !dto.getActualCheckOutTime().equals(booking.getActualCheckOutTime())) {
            changes.append("actualCheckOutTime: ").append(booking.getActualCheckOutTime())
                    .append(" → ").append(dto.getActualCheckOutTime()).append("; ");
            booking.setActualCheckOutTime(dto.getActualCheckOutTime());
        }

        if (dto.getBookingType() != null && !dto.getBookingType().equals(booking.getBookingType())) {
            changes.append("bookingType: ").append(booking.getBookingType())
                    .append(" → ").append(dto.getBookingType()).append("; ");
            booking.setBookingType(dto.getBookingType());
        }

        if (dto.getStatus() != null && !dto.getStatus().equals(booking.getStatus())) {
            changes.append("status: ").append(booking.getStatus())
                    .append(" → ").append(dto.getStatus()).append("; ");
            booking.setStatus(dto.getStatus());
        }

        if (dto.getNumberOfGuests() != null && dto.getNumberOfGuests() != booking.getNumberOfGuests()) {
            changes.append("numberOfGuests: ").append(booking.getNumberOfGuests())
                    .append(" → ").append(dto.getNumberOfGuests()).append("; ");
            booking.setNumberOfGuests(dto.getNumberOfGuests());
        }

        if (dto.getNotes() != null && !dto.getNotes().equals(booking.getNotes())) {
            changes.append("notes: ").append(booking.getNotes())
                    .append(" → ").append(dto.getNotes()).append("; ");
            booking.setNotes(dto.getNotes());
        }

        if (dto.getCancelReason() != null && !dto.getCancelReason().equals(booking.getCancelReason())) {
            changes.append("cancelReason: ").append(booking.getCancelReason())
                    .append(" → ").append(dto.getCancelReason()).append("; ");
            booking.setCancelReason(dto.getCancelReason());
        }

        Booking updatedBooking = bookingRepository.save(booking);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        auditLogService.log(
                username,
                "UPDATE",
                "Booking",
                updatedBooking.getId(),
                changes.toString()
        );

        return BookingMapper.toDto(updatedBooking);
    }

    @Override
    public void deleteBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
        if (invoiceRepository.existsByBookingId(id)) {
            throw new BookingException.BookingHasInvoiceException(id);
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        String details = "Deleted booking with ID: " + booking.getId() +
                ", guest: " + booking.getGuestFullName() +
                ", roomNumber: " + (booking.getRoom() != null ? booking.getRoom().getRoomNumber() : "null") +
                ", checkIn: " + booking.getCheckInDate() +
                ", checkOut: " + booking.getCheckOutDate() +
                ", status: " + booking.getStatus();

        auditLogService.log(
                username,
                "DELETE",
                "Booking",
                booking.getId(),
                details
        );
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
        query.orderBy(cb.desc(booking.get("id")));

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

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

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        StringBuilder details = new StringBuilder("Filtered bookings with criteria: ");
        if (filter.getGuestFullName() != null) details.append("guestFullName=").append(filter.getGuestFullName()).append("; ");
        if (filter.getGuestIdNumber() != null) details.append("guestIdNumber=").append(filter.getGuestIdNumber()).append("; ");
        if (filter.getGuestNationality() != null) details.append("guestNationality=").append(filter.getGuestNationality()).append("; ");
        if (filter.getStatus() != null) details.append("status=").append(filter.getStatus()).append("; ");
        if (filter.getBookingType() != null) details.append("bookingType=").append(filter.getBookingType()).append("; ");
        if (filter.getCheckInDateFrom() != null) details.append("checkInDateFrom=").append(filter.getCheckInDateFrom()).append("; ");
        if (filter.getCheckInDateTo() != null) details.append("checkInDateTo=").append(filter.getCheckInDateTo()).append("; ");
        if (filter.getCheckOutDateFrom() != null) details.append("checkOutDateFrom=").append(filter.getCheckOutDateFrom()).append("; ");
        if (filter.getCheckOutDateTo() != null) details.append("checkOutDateTo=").append(filter.getCheckOutDateTo()).append("; ");
        if (filter.getRoomId() != null) details.append("roomNo=").append(filter.getRoomId()).append("; ");
        details.append("page=").append(page).append(", size=").append(size)
                .append(", total results=").append(total);

        auditLogService.log(
                username,
                "SEARCH",
                "Booking",
                null,
                details.toString()
        );
        return bookingsPage.map(BookingMapper::toDto);
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Booking> root, BookingFilterRequest filter) {
        List<Predicate> predicates = new ArrayList<>();

        if (filter.getGuestFullName() != null && !filter.getGuestFullName().isBlank()) {
            predicates.add(cb.like(
                    cb.lower(root.get("guestFullName")),
                    "%" + filter.getGuestFullName().toLowerCase() + "%"
            ));
        }

        if (filter.getGuestIdNumber() != null && !filter.getGuestIdNumber().isBlank()) {
            predicates.add(cb.equal(root.get("guestIdNumber"), filter.getGuestIdNumber()));
        }

        if (filter.getGuestNationality() != null && !filter.getGuestNationality().isBlank()) {
            predicates.add(cb.equal(root.get("guestNationality"), filter.getGuestNationality()));
        }

        if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
            predicates.add(cb.equal(root.get("status"), filter.getStatus()));
        }

        if (filter.getBookingType() != null && !filter.getBookingType().isBlank()) {
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
            predicates.add(cb.equal(root.get("room").get("roomNumber"), filter.getRoomId()));
        }

        return predicates;
    }

    @Scheduled(fixedRate = 3600000)
    @Override
    public void notifyRoomsAboutUpcomingCheckout() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusMonths(1);

        List<Booking> upcomingBookings = bookingRepository.findByCheckOutDateBetween(now, threshold);

        if (upcomingBookings.isEmpty()) return;

        List<User> receptionists = userRepository.findByRoles_Name("RECEPTIONIST");

        for (Booking booking : upcomingBookings) {
            Room room = booking.getRoom();
            if (room == null) {
                continue;
            }

            String title = "Upcoming Room Checkout";
            String message = String.format(
                    "Room %s is scheduled to check out at %s",
                    room.getRoomNumber(),
                    booking.getCheckOutDate().format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"))
            );

            for (User receptionist : receptionists) {
                notificationService.sendNotification(
                        receptionist,
                        "ROOM_CHECKOUT_REMINDER",
                        title,
                        message
                );
            }
        }
    }

}

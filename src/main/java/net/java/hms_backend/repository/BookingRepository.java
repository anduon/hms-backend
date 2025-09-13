package net.java.hms_backend.repository;

import net.java.hms_backend.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b " +
            "WHERE b.room.id = :roomId " +
            "AND b.checkInDate < :newCheckOut " +
            "AND b.checkOutDate > :newCheckIn")
    List<Booking> findOverlappingBookings(
            @Param("roomId") Long roomId,
            @Param("newCheckIn") LocalDateTime newCheckIn,
            @Param("newCheckOut") LocalDateTime newCheckOut
    );

    Long countByStatus(String status);

    List<Booking> findByStatusAndCheckOutDateAfter(String status, LocalDateTime date);

    List<Booking> findByActualCheckInTimeIsNotNull();

    List<Booking> findByActualCheckOutTimeBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);

    List<Booking> findByCheckInDateBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);

    List<Booking> findByCheckOutDateBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);

    @Query("SELECT SUM(b.numberOfGuests) FROM Booking b WHERE b.checkInDate BETWEEN :startDate AND :endDate")
    Long countTotalGuestsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    List<Booking> findByCheckInDateAfter(LocalDateTime date);
}

package net.java.hms_backend.repository;

import net.java.hms_backend.entity.BookingExtraCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingExtraChargeRepository extends JpaRepository<BookingExtraCharge, Long> {
    List<BookingExtraCharge> findByBookingId(Long bookingId);
}

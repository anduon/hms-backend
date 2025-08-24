package net.java.hms_backend.repository;

import net.java.hms_backend.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Invoice findByBookingId(Long bookingId);
}

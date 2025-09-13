package net.java.hms_backend.repository;

import net.java.hms_backend.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Invoice findByBookingId(Long bookingId);

    Long countByStatus(String status);

    List<Invoice> findByStatus(String status);

    @Query("SELECT SUM(i.paidAmount) FROM Invoice i WHERE i.status = :status")
    BigDecimal sumPaidAmountByStatus(@Param("status") String status);

    @Query("SELECT SUM(i.amount - i.paidAmount) FROM Invoice i WHERE i.status = :status AND i.amount > i.paidAmount")
    BigDecimal sumOutstandingAmountByStatus(@Param("status") String status);

    List<Invoice> findByIssuedDateAfter(LocalDateTime date);
}

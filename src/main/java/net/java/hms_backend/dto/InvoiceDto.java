package net.java.hms_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDto {
    private Long id;
    private Long bookingId;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private String status;
    private LocalDateTime issuedDate;
    private LocalDateTime dueDate;
    private String paymentMethod;
    private String notes;
}

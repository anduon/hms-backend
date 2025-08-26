package net.java.hms_backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InvoiceFilterRequest {
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String status;
    private LocalDateTime issuedDateFrom;
    private LocalDateTime issuedDateTo;
    private LocalDateTime dueDateFrom;
    private LocalDateTime dueDateTo;
    private String paymentMethod;
    private Long bookingId;
}

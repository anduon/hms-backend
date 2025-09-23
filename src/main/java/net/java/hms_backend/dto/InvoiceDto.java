package net.java.hms_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDto implements AuditDto {
    private Long id;
    private Long bookingId;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private String status;
    private LocalDateTime issuedDate;
    private LocalDateTime dueDate;
    private String paymentMethod;
    private String notes;

    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

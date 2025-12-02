package net.java.hms_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingExtraChargeDto {

    private Long id;
    private Long bookingId;
    private Long extraChargeId;
    private int quantity;
    private BigDecimal totalPrice;
    private String note;
}


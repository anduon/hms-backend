package net.java.hms_backend.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PromotionFilterRequest {
    private String name;
    private Double minDiscount;
    private Double maxDiscount;
    private LocalDate startDateFrom;
    private LocalDate startDateTo;
    private LocalDate endDateFrom;
    private LocalDate endDateTo;
}

package net.java.hms_backend.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PromotionDto {
    private Long id;
    private String name;
    private Double discountPercent;
    private LocalDate startDate;
    private LocalDate endDate;
}

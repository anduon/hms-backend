package net.java.hms_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PromotionDto {

    private Long id;

    @NotBlank(message = "Promotion name is required")
    private String name;

    @NotNull(message = "Discount percent is required")
    private Double discountPercent;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;
}

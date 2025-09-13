package net.java.hms_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetDto {
    private Long id;
    private String name;
    private String category;
    private String condition;
    private Double originalCost;
    private LocalDate purchaseDate;
    private String note;
    @NotNull(message = "Room number is required")
    private Integer roomNumber;
}

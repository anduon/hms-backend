package net.java.hms_backend.dto;

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
    private Integer roomNumber;
}

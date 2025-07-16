package net.java.hms_backend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AssetFilterRequestDto {
    private String name;
    private String category;
    private String condition;
    private Double minCost;
    private Double maxCost;
    private LocalDate purchaseDateFrom;
    private LocalDate purchaseDateTo;
    private Integer roomNumber;
}

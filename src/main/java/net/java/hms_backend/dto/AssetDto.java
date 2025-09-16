package net.java.hms_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetDto implements AuditDto{
    private Long id;
    private String name;
    private String category;
    private String condition;
    private Double originalCost;
    private LocalDate purchaseDate;
    private String note;
    @NotNull(message = "Room number is required")
    private Integer roomNumber;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

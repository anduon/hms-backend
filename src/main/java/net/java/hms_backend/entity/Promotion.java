package net.java.hms_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import net.java.hms_backend.entity.base.Auditable;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "promotion")
public class Promotion extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Double discountPercent;

    private LocalDate startDate;
    private LocalDate endDate;
}

package net.java.hms_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.java.hms_backend.entity.base.Auditable;

import java.math.BigDecimal;

@Entity
@Table(name = "extra_charges")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtraCharge extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String type;

    private BigDecimal price;

    private String description;
}

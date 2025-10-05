package net.java.hms_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "booking_extra_charges")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingExtraCharge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "extra_charge_id")
    private ExtraCharge extraCharge;

    private int quantity;

    private BigDecimal totalPrice;

    private String note;
}


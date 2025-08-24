package net.java.hms_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.java.hms_backend.entity.PriceType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomPriceDto {
    private PriceType priceType;
    private Double basePrice;
}

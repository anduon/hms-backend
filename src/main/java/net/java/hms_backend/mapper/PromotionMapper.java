package net.java.hms_backend.mapper;

import net.java.hms_backend.dto.PromotionDto;
import net.java.hms_backend.entity.Promotion;

public class PromotionMapper {

    public static PromotionDto toDto(Promotion entity) {
        return new PromotionDto(
                entity.getId(),
                entity.getName(),
                entity.getDiscountPercent(),
                entity.getStartDate(),
                entity.getEndDate()
        );
    }

    public static Promotion toEntity(PromotionDto dto) {
        Promotion promotion = new Promotion();
        promotion.setId(dto.getId());
        promotion.setName(dto.getName());
        promotion.setDiscountPercent(dto.getDiscountPercent());
        promotion.setStartDate(dto.getStartDate());
        promotion.setEndDate(dto.getEndDate());
        return promotion;
    }
}

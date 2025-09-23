package net.java.hms_backend.mapper;

import net.java.hms_backend.dto.PromotionDto;
import net.java.hms_backend.entity.Promotion;

public class PromotionMapper {

    public static PromotionDto toDto(Promotion entity) {
        PromotionDto dto = new PromotionDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDiscountPercent(entity.getDiscountPercent());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        BaseMapper.mapAuditFields(entity, dto);

        return dto;
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

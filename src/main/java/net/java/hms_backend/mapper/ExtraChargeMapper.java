package net.java.hms_backend.mapper;

import net.java.hms_backend.dto.ExtraChargeDto;
import net.java.hms_backend.entity.ExtraCharge;

public class ExtraChargeMapper {

    public static ExtraChargeDto toDto(ExtraCharge entity) {
        ExtraChargeDto dto = new ExtraChargeDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setType(entity.getType());
        dto.setPrice(entity.getPrice());
        dto.setDescription(entity.getDescription());
        BaseMapper.mapAuditFields(entity, dto);
        return dto;
    }

    public static ExtraCharge toEntity(ExtraChargeDto dto) {
        ExtraCharge entity = new ExtraCharge();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setType(dto.getType());
        entity.setPrice(dto.getPrice());
        entity.setDescription(dto.getDescription());
        return entity;
    }
}


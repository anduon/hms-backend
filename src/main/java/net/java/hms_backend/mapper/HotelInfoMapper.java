package net.java.hms_backend.mapper;

import net.java.hms_backend.dto.HotelInfoDto;
import net.java.hms_backend.entity.HotelInfo;

public class HotelInfoMapper {

    public static HotelInfoDto toDto(HotelInfo entity) {
        HotelInfoDto dto = new HotelInfoDto();
        dto.setName(entity.getName());
        dto.setAddress(entity.getAddress());
        dto.setPhone(entity.getPhone());
        dto.setEmail(entity.getEmail());
        dto.setTaxCode(entity.getTaxCode());
        dto.setNumberOfFloors(entity.getNumberOfFloors());
        dto.setCheckInTime(entity.getCheckInTime());
        dto.setCheckOutTime(entity.getCheckOutTime());
        return dto;
    }

    public static HotelInfo toEntity(HotelInfoDto dto) {
        HotelInfo entity = new HotelInfo();
        entity.setName(dto.getName());
        entity.setAddress(dto.getAddress());
        entity.setPhone(dto.getPhone());
        entity.setEmail(dto.getEmail());
        entity.setTaxCode(dto.getTaxCode());
        entity.setNumberOfFloors(dto.getNumberOfFloors());
        entity.setCheckInTime(dto.getCheckInTime());
        entity.setCheckOutTime(dto.getCheckOutTime());
        return entity;
    }
}

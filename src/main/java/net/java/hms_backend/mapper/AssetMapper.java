package net.java.hms_backend.mapper;

import net.java.hms_backend.dto.AssetDto;
import net.java.hms_backend.entity.Asset;
import net.java.hms_backend.entity.Room;

public class AssetMapper {

    public static AssetDto toDto(Asset asset) {
        AssetDto dto = new AssetDto();
        dto.setId(asset.getId());
        dto.setName(asset.getName());
        dto.setCategory(asset.getCategory());
        dto.setCondition(asset.getCondition());
        dto.setOriginalCost(asset.getOriginalCost());
        dto.setPurchaseDate(asset.getPurchaseDate());
        dto.setNote(asset.getNote());
        dto.setRoomNumber(asset.getRoom().getRoomNumber());

        BaseMapper.mapAuditFields(asset, dto);

        return dto;
    }


    public static Asset toEntity(AssetDto dto, Room room) {
        return new Asset(
                dto.getId(),
                dto.getName(),
                dto.getCategory(),
                dto.getCondition(),
                dto.getOriginalCost(),
                dto.getPurchaseDate(),
                dto.getNote(),
                room
        );
    }
}

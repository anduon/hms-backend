package net.java.hms_backend.mapper;

import net.java.hms_backend.dto.AssetDto;
import net.java.hms_backend.entity.Asset;
import net.java.hms_backend.entity.Room;

public class AssetMapper {

    public static AssetDto toDto(Asset asset) {
        return new AssetDto(
                asset.getId(),
                asset.getName(),
                asset.getCategory(),
                asset.getCondition(),
                asset.getOriginalCost(),
                asset.getPurchaseDate(),
                asset.getNote(),
                asset.getRoom().getRoomNumber()
        );
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

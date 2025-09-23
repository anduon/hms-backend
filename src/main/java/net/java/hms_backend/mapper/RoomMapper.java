package net.java.hms_backend.mapper;

import net.java.hms_backend.dto.RoomDto;
import net.java.hms_backend.dto.RoomPriceDto;
import net.java.hms_backend.entity.Promotion;
import net.java.hms_backend.entity.Room;
import net.java.hms_backend.entity.RoomPrice;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomMapper {

    public static RoomDto mapToRoomDto(Room room) {
        RoomDto dto = new RoomDto();
        dto.setId(room.getId());
        dto.setRoomNumber(room.getRoomNumber());
        dto.setMaxOccupancy(room.getMaxOccupancy());
        dto.setRoomType(room.getRoomType());
        dto.setStatus(room.getStatus());
        dto.setLocation(room.getLocation());

        List<RoomPriceDto> priceDtos = new ArrayList<>();
        if (room.getPrices() != null) {
            for (RoomPrice price : room.getPrices()) {
                RoomPriceDto priceDto = new RoomPriceDto();
                priceDto.setPriceType(price.getPriceType());
                priceDto.setBasePrice(price.getBasePrice());
                priceDtos.add(priceDto);
            }
        }
        dto.setPrices(priceDtos);
        BaseMapper.mapAuditFields(room, dto);
        return dto;
    }

    public static RoomDto mapToRoomDto(Room room, Optional<Promotion> promotionOpt) {
        RoomDto dto = new RoomDto();
        dto.setId(room.getId());
        dto.setRoomNumber(room.getRoomNumber());
        dto.setMaxOccupancy(room.getMaxOccupancy());
        dto.setRoomType(room.getRoomType());
        dto.setStatus(room.getStatus());
        dto.setLocation(room.getLocation());

        List<RoomPriceDto> priceDtos = new ArrayList<>();
        for (RoomPrice price : room.getPrices()) {
            double finalPrice = price.getBasePrice();

            if (promotionOpt.isPresent()) {
                double discount = promotionOpt.get().getDiscountPercent() / 100.0;
                finalPrice *= (1 - discount);
            }

            RoomPriceDto priceDto = new RoomPriceDto();
            priceDto.setPriceType(price.getPriceType());
            priceDto.setBasePrice(finalPrice);
            priceDtos.add(priceDto);
        }

        dto.setPrices(priceDtos);
        return dto;
    }


    public static Room mapToRoom(RoomDto dto) {
        Room room = new Room();
        room.setId(dto.getId());
        room.setRoomNumber(dto.getRoomNumber());
        room.setMaxOccupancy(dto.getMaxOccupancy());
        room.setRoomType(dto.getRoomType());
        room.setStatus(dto.getStatus());
        room.setLocation(dto.getLocation());

        List<RoomPrice> prices = new ArrayList<>();
        if (dto.getPrices() != null) {
            for (RoomPriceDto priceDto : dto.getPrices()) {
                RoomPrice price = new RoomPrice();
                price.setPriceType(priceDto.getPriceType());
                price.setBasePrice(priceDto.getBasePrice());
                price.setRoom(room);
                prices.add(price);
            }
        }
        room.setPrices(prices);

        return room;
    }
}

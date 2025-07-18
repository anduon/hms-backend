package net.java.hms_backend.service.impl;

import lombok.AllArgsConstructor;
import net.java.hms_backend.dto.RoomDto;
import net.java.hms_backend.entity.Promotion;
import net.java.hms_backend.entity.Room;
import net.java.hms_backend.exception.ResourceNotFoundException;
import net.java.hms_backend.mapper.RoomMapper;
import net.java.hms_backend.repository.RoomRepository;
import net.java.hms_backend.service.PromotionService;
import net.java.hms_backend.service.RoomService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RoomServiceImpl implements RoomService {

    private RoomRepository roomRepository;
    private final PromotionService promotionService;

    @Override
    public RoomDto createRoom(RoomDto roomDto) {
        Room room = RoomMapper.mapToRoom(roomDto);
        Room savedRoom = roomRepository.save(room);
        return RoomMapper.mapToRoomDto(savedRoom);
    }

    @Override
    public List<RoomDto> getAllRooms() {
        Optional<Promotion> promotionOpt = promotionService.getActivePromotion();
        return roomRepository.findAll().stream()
                .map(room -> RoomMapper.mapToRoomDto(room, promotionOpt))
                .collect(Collectors.toList());
    }


    @Override
    public RoomDto getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));
        return RoomMapper.mapToRoomDto(room);
    }

    @Override
    public RoomDto updateRoom(Long id, RoomDto roomDto) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));

        room.setRoomNumber(roomDto.getRoomNumber());
        room.setMaxOccupancy(roomDto.getMaxOccupancy());
        room.setRoomType(roomDto.getRoomType());
        room.setStatus(roomDto.getStatus());
        room.setLocation(roomDto.getLocation());

        if (roomDto.getPrices() != null) {
            for (var priceDto : roomDto.getPrices()) {
                var existingPriceOpt = room.getPrices().stream()
                        .filter(p -> p.getPriceType() == priceDto.getPriceType())
                        .findFirst();

                if (existingPriceOpt.isPresent()) {
                    existingPriceOpt.get().setBasePrice(priceDto.getBasePrice());
                } else {
                    var newPrice = new net.java.hms_backend.entity.RoomPrice();
                    newPrice.setPriceType(priceDto.getPriceType());
                    newPrice.setBasePrice(priceDto.getBasePrice());
                    newPrice.setRoom(room);
                    room.getPrices().add(newPrice);
                }
            }
        }

        Room updatedRoom = roomRepository.save(room);
        return RoomMapper.mapToRoomDto(updatedRoom);
    }



    @Override
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));
        roomRepository.delete(room);
    }
}

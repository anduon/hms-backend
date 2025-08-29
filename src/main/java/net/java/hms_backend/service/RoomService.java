package net.java.hms_backend.service;

import net.java.hms_backend.dto.RoomDto;
import net.java.hms_backend.dto.RoomFilterRequest;
import org.springframework.data.domain.Page;

public interface RoomService {
    RoomDto createRoom(RoomDto roomDto);

    Page<RoomDto> getAllRooms(int page, int size);

    RoomDto getRoomById(Long id);

    RoomDto updateRoom(Long id, RoomDto roomDto);

    void deleteRoom(Long id);

    Page<RoomDto> filterRooms(RoomFilterRequest request, int page, int size);


}

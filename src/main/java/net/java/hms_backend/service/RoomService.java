package net.java.hms_backend.service;

import net.java.hms_backend.dto.RoomDto;
import net.java.hms_backend.dto.RoomFilterRequest;

import java.util.List;

public interface RoomService {
    RoomDto createRoom(RoomDto roomDto);

    List<RoomDto> getAllRooms();

    RoomDto getRoomById(Long id);

    RoomDto updateRoom(Long id, RoomDto roomDto);

    void deleteRoom(Long id);

}

package net.java.hms_backend.service.impl;

import lombok.AllArgsConstructor;
import net.java.hms_backend.dto.RoomDto;
import net.java.hms_backend.entity.Room;
import net.java.hms_backend.mapper.RoomMapper;
import net.java.hms_backend.repository.RoomRepository;
import net.java.hms_backend.service.RoomService;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RoomServiceImpl implements RoomService {

    private RoomRepository roomRepository;

    @Override
    public RoomDto createRoom(RoomDto roomDto) {
        Room room = RoomMapper.mapToRoom(roomDto);
        Room savedRoom = roomRepository.save(room);
        return RoomMapper.mapToRoomDto(savedRoom);
    }
}

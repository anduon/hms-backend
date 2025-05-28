package net.java.hms_backend.mapper;

import net.java.hms_backend.dto.RoomDto;
import net.java.hms_backend.entity.Room;

public class RoomMapper {

    public static RoomDto mapToRoomDto(Room room){
        return new RoomDto(
                room.getId(),
                room.getRoomNumber(),
                room.getMaxOccupancy(),
                room.getRoomType(),
                room.getPrice(),
                room.getStatus(),
                room.getLocation()
        );
    }

    public static Room mapToRoom(RoomDto roomDto){
        return new Room(
                roomDto.getId(),
                roomDto.getRoomNumber(),
                roomDto.getMaxOccupancy(),
                roomDto.getRoomType(),
                roomDto.getPrice(),
                roomDto.getStatus(),
                roomDto.getLocation()
        );
    }
}

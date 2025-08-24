package net.java.hms_backend.controller;

import lombok.RequiredArgsConstructor;
import net.java.hms_backend.dto.RoomDto;
import net.java.hms_backend.dto.RoomFilterRequest;
import net.java.hms_backend.service.RoomService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Validated
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<RoomDto> createRoom(@RequestBody @Validated RoomDto roomDto) {
        RoomDto savedRoom = roomService.createRoom(roomDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRoom);
    }

    @GetMapping
    public ResponseEntity<Page<RoomDto>> getAllRooms(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        Page<RoomDto> rooms = roomService.getAllRooms(page, size);
        return ResponseEntity.ok(rooms);
    }


    @GetMapping("/{id}")
    public ResponseEntity<RoomDto> getRoomById(@PathVariable("id") Long id) {
        RoomDto room = roomService.getRoomById(id);
        return ResponseEntity.ok(room);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomDto> updateRoom(@PathVariable("id") Long id,
                                              @RequestBody @Validated RoomDto roomDto) {
        RoomDto updatedRoom = roomService.updateRoom(id, roomDto);
        return ResponseEntity.ok(updatedRoom);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRoom(@PathVariable("id") Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok("Room deleted successfully.");
    }

    @PostMapping("/filter")
    public ResponseEntity<Page<RoomDto>> filterRooms(
            @RequestBody RoomFilterRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<RoomDto> rooms = roomService.filterRooms(request, page, size);
        return ResponseEntity.ok(rooms);
    }



}

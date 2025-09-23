package net.java.hms_backend.controller;

import lombok.AllArgsConstructor;
import net.java.hms_backend.dto.HotelInfoDto;
import net.java.hms_backend.entity.HotelInfo;
import net.java.hms_backend.service.HotelInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hotel-info")
@AllArgsConstructor
public class HotelInfoController {

    private final HotelInfoService hotelInfoService;

    @GetMapping
    public ResponseEntity<HotelInfoDto> getHotelInfo() {
        return ResponseEntity.ok(hotelInfoService.getHotelInfo());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HotelInfoDto> updateHotelInfo(@RequestBody HotelInfoDto dto) {
        return ResponseEntity.ok(hotelInfoService.updateHotelInfo(dto));
    }

}


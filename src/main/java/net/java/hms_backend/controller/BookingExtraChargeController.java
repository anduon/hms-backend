package net.java.hms_backend.controller;

import lombok.RequiredArgsConstructor;
import net.java.hms_backend.dto.BookingExtraChargeDto;
import net.java.hms_backend.service.BookingExtraChargeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/booking-extra-charges")
@RequiredArgsConstructor
public class BookingExtraChargeController {

    private final BookingExtraChargeService service;

    @PostMapping
    public ResponseEntity<BookingExtraChargeDto> create(@RequestBody BookingExtraChargeDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingExtraChargeDto> update(@PathVariable Long id, @RequestBody BookingExtraChargeDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingExtraChargeDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<BookingExtraChargeDto>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<BookingExtraChargeDto>> getByBookingId(@PathVariable Long bookingId) {
        return ResponseEntity.ok(service.getByBookingId(bookingId));
    }

}

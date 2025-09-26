package net.java.hms_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.java.hms_backend.dto.PromotionDto;
import net.java.hms_backend.dto.PromotionFilterRequest;
import net.java.hms_backend.entity.Promotion;
import net.java.hms_backend.mapper.PromotionMapper;
import net.java.hms_backend.service.PromotionService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping
    public ResponseEntity<PromotionDto> createPromotion(@Valid @RequestBody PromotionDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(promotionService.createPromotion(dto));
    }

    @GetMapping
    public ResponseEntity<Page<PromotionDto>> getAllPromotions(@RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(promotionService.getAllPromotions(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromotionDto> getPromotionById(@PathVariable Long id) {
        return ResponseEntity.ok(promotionService.getPromotionById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromotionDto> updatePromotion(@PathVariable Long id, @RequestBody PromotionDto dto) {
        return ResponseEntity.ok(promotionService.updatePromotion(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePromotion(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.ok("Promotion deleted successfully.");
    }

    @GetMapping("/booking-promotion")
    public ResponseEntity<PromotionDto> getPromotionForBooking(
            @RequestParam("checkIn") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkIn,
            @RequestParam("checkOut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkOut) {

        Optional<Promotion> promotionOpt = promotionService.getPromotionForBooking(checkIn, checkOut);

        return promotionOpt
                .map(promotion -> ResponseEntity.ok(PromotionMapper.toDto(promotion)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/search")
    public ResponseEntity<Page<PromotionDto>> searchPromotions(
            @RequestBody PromotionFilterRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(promotionService.searchPromotions(request, page, size));
    }
}

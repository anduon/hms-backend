package net.java.hms_backend.service;

import net.java.hms_backend.dto.PromotionDto;
import net.java.hms_backend.entity.Promotion;

import java.util.List;
import java.util.Optional;

public interface PromotionService {
    Optional<Promotion> getActivePromotion();
    PromotionDto createPromotion(PromotionDto dto);
    List<PromotionDto> getAllPromotions();
    PromotionDto getPromotionById(Long id);
    PromotionDto updatePromotion(Long id, PromotionDto dto);
    void deletePromotion(Long id);
}
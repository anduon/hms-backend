package net.java.hms_backend.service;

import net.java.hms_backend.dto.PromotionDto;
import net.java.hms_backend.dto.PromotionFilterRequest;
import net.java.hms_backend.entity.Promotion;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface PromotionService {
    Optional<Promotion> getActivePromotion();
    PromotionDto createPromotion(PromotionDto dto);
    Page<PromotionDto> getAllPromotions(int page, int size);
    PromotionDto getPromotionById(Long id);
    PromotionDto updatePromotion(Long id, PromotionDto dto);
    void deletePromotion(Long id);
    Page<PromotionDto> searchPromotions(PromotionFilterRequest request, int page, int size);
}
package net.java.hms_backend.service.impl;

import lombok.RequiredArgsConstructor;
import net.java.hms_backend.dto.PromotionDto;
import net.java.hms_backend.entity.Promotion;
import net.java.hms_backend.exception.ResourceNotFoundException;
import net.java.hms_backend.mapper.PromotionMapper;
import net.java.hms_backend.repository.PromotionRepository;
import net.java.hms_backend.service.PromotionService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;

    @Override
    public Optional<Promotion> getActivePromotion() {
        LocalDate today = LocalDate.now();
        List<Promotion> promotions = promotionRepository
                .findByStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today);
        return promotions.stream()
                .max(Comparator.comparing(Promotion::getDiscountPercent));

    }

    @Override
    public PromotionDto createPromotion(PromotionDto dto) {
        Promotion entity = PromotionMapper.toEntity(dto);
        Promotion saved = promotionRepository.save(entity);
        return PromotionMapper.toDto(saved);
    }

    @Override
    public List<PromotionDto> getAllPromotions() {
        return promotionRepository.findAll()
                .stream()
                .map(PromotionMapper::toDto)
                .toList();
    }

    @Override
    public PromotionDto getPromotionById(Long id) {
        Promotion promo = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", id));
        return PromotionMapper.toDto(promo);
    }

    @Override
    public PromotionDto updatePromotion(Long id, PromotionDto dto) {
        Promotion promo = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", id));
        promo.setName(dto.getName());
        promo.setDiscountPercent(dto.getDiscountPercent());
        promo.setStartDate(dto.getStartDate());
        promo.setEndDate(dto.getEndDate());

        Promotion updated = promotionRepository.save(promo);
        return PromotionMapper.toDto(updated);
    }

    @Override
    public void deletePromotion(Long id) {
        Promotion promo = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", id));
        promotionRepository.delete(promo);
    }
}

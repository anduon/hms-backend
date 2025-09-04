package net.java.hms_backend.service.impl;

import lombok.RequiredArgsConstructor;
import net.java.hms_backend.dto.PromotionDto;
import net.java.hms_backend.dto.PromotionFilterRequest;
import net.java.hms_backend.entity.Promotion;
import net.java.hms_backend.exception.ResourceNotFoundException;
import net.java.hms_backend.mapper.PromotionMapper;
import net.java.hms_backend.repository.PromotionRepository;
import net.java.hms_backend.service.PromotionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
    public Page<PromotionDto> getAllPromotions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Promotion> promotionsPage = promotionRepository.findAll(pageable);
        return promotionsPage.map(PromotionMapper::toDto);
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
        if (dto.getName() != null) {
            promo.setName(dto.getName());
        }
        if (dto.getDiscountPercent() != null) {
            promo.setDiscountPercent(dto.getDiscountPercent());
        }
        if (dto.getStartDate() != null) {
            promo.setStartDate(dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            promo.setEndDate(dto.getEndDate());
        }
        Promotion updated = promotionRepository.save(promo);
        return PromotionMapper.toDto(updated);
    }


    @Override
    public void deletePromotion(Long id) {
        Promotion promo = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", id));
        promotionRepository.delete(promo);
    }

    @Override
    public Page<PromotionDto> searchPromotions(PromotionFilterRequest request, int page, int size) {
        List<Promotion> promotions = promotionRepository.findAll();
        Stream<Promotion> stream = promotions.stream();

        if (request.getName() != null && !request.getName().isEmpty()) {
            stream = stream.filter(p -> p.getName().toLowerCase().contains(request.getName().toLowerCase()));
        }

        if (request.getMinDiscount() != null) {
            stream = stream.filter(p -> p.getDiscountPercent() >= request.getMinDiscount());
        }

        if (request.getMaxDiscount() != null) {
            stream = stream.filter(p -> p.getDiscountPercent() <= request.getMaxDiscount());
        }

        if (request.getStartDateFrom() != null) {
            stream = stream.filter(p -> !p.getStartDate().isBefore(request.getStartDateFrom()));
        }

        if (request.getStartDateTo() != null) {
            stream = stream.filter(p -> !p.getStartDate().isAfter(request.getStartDateTo()));
        }

        if (request.getEndDateFrom() != null) {
            stream = stream.filter(p -> !p.getEndDate().isBefore(request.getEndDateFrom()));
        }

        if (request.getEndDateTo() != null) {
            stream = stream.filter(p -> !p.getEndDate().isAfter(request.getEndDateTo()));
        }

        List<Promotion> filteredPromotions = stream.toList();

        Pageable pageable = PageRequest.of(page, size);
        int start = Math.min((int) pageable.getOffset(), filteredPromotions.size());
        int end = Math.min((start + pageable.getPageSize()), filteredPromotions.size());

        List<Promotion> pagedPromotions = filteredPromotions.subList(start, end);

        Page<Promotion> promotionPage = new PageImpl<>(pagedPromotions, pageable, filteredPromotions.size());

        return promotionPage.map(PromotionMapper::toDto);
    }

}

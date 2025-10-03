package net.java.hms_backend.service.impl;

import lombok.RequiredArgsConstructor;
import net.java.hms_backend.dto.PromotionDto;
import net.java.hms_backend.dto.PromotionFilterRequest;
import net.java.hms_backend.entity.Promotion;
import net.java.hms_backend.exception.PromotionException;
import net.java.hms_backend.exception.ResourceNotFoundException;
import net.java.hms_backend.mapper.PromotionMapper;
import net.java.hms_backend.repository.PromotionRepository;
import net.java.hms_backend.service.AuditLogService;
import net.java.hms_backend.service.PromotionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final AuditLogService auditLogService;

    @Override
    public Optional<Promotion> getActivePromotion() {
        LocalDate today = LocalDate.now();
        List<Promotion> promotions = promotionRepository
                .findByStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today);

        Optional<Promotion> activePromotion = promotions.stream()
                .max(Comparator.comparing(Promotion::getDiscountPercent));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (activePromotion.isPresent()) {
            Promotion promo = activePromotion.get();
            String details = "Retrieved active promotion on " + today +
                    ": [ID=" + promo.getId() +
                    ", Name=" + promo.getName() +
                    ", Discount=" + promo.getDiscountPercent() + "%]";
            auditLogService.log(username, "QUERY", "Promotion", promo.getId(), details);
        } else {
            String details = "Retrieved active promotion on " + today + ": No active promotion found.";
            auditLogService.log(username, "QUERY", "Promotion", null, details);
        }

        return activePromotion;
    }


    @Override
    public Optional<Promotion> getPromotionForBooking(LocalDateTime checkIn, LocalDateTime checkOut) {
        Optional<Promotion> promotion = promotionRepository.findAll().stream()
                .filter(p -> !p.getEndDate().isBefore(checkIn.toLocalDate()) && !p.getStartDate().isAfter(checkOut.toLocalDate()))
                .max(Comparator.comparingDouble(Promotion::getDiscountPercent));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        StringBuilder details = new StringBuilder("Checked promotion for booking from ")
                .append(checkIn.toLocalDate()).append(" to ").append(checkOut.toLocalDate());

        if (promotion.isPresent()) {
            Promotion promo = promotion.get();
            details.append("; Selected promotion: [ID=").append(promo.getId())
                    .append(", Name=").append(promo.getName())
                    .append(", Discount=").append(promo.getDiscountPercent()).append("%]");
            auditLogService.log(username, "QUERY", "Promotion", promo.getId(), details.toString());
        } else {
            details.append("; No applicable promotion found.");
            auditLogService.log(username, "QUERY", "Promotion", null, details.toString());
        }

        return promotion;
    }



    @Override
    public PromotionDto createPromotion(PromotionDto dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new PromotionException.MissingNameException();
        }

        if (dto.getDiscountPercent() == null) {
            throw new PromotionException.MissingDiscountPercentException();
        }

        if (dto.getDiscountPercent() < 0 || dto.getDiscountPercent() > 100) {
            throw new PromotionException.InvalidDiscountRangeException();
        }

        if (dto.getStartDate() == null) {
            throw new PromotionException.MissingStartDateException();
        }

        if (dto.getEndDate() == null) {
            throw new PromotionException.MissingEndDateException();
        }

        if (!dto.getEndDate().isAfter(dto.getStartDate())) {
            throw new PromotionException.InvalidDateRangeException();
        }

        Promotion entity = PromotionMapper.toEntity(dto);
        Promotion saved = promotionRepository.save(entity);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String details = "Created promotion: [ID=" + saved.getId() +
                ", Name=" + saved.getName() +
                ", Discount=" + saved.getDiscountPercent() + "%" +
                ", Start=" + saved.getStartDate() +
                ", End=" + saved.getEndDate() + "]";

        auditLogService.log(username, "CREATE", "Promotion", saved.getId(), details);
        return PromotionMapper.toDto(saved);
    }


    @Override
    public Page<PromotionDto> getAllPromotions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Promotion> promotionsPage = promotionRepository.findAll(pageable);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String details = "Retrieved promotion list - Page: " + page + ", Size: " + size +
                ", Total: " + promotionsPage.getTotalElements();

        auditLogService.log(username, "QUERY", "Promotion", null, details);
        return promotionsPage.map(PromotionMapper::toDto);
    }


    @Override
    public PromotionDto getPromotionById(Long id) {
        Promotion promo = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", id));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String details = "Retrieved promotion by ID: " + id +
                ", Name: " + promo.getName() +
                ", Discount: " + promo.getDiscountPercent() + "%" +
                ", Start: " + promo.getStartDate() +
                ", End: " + promo.getEndDate();

        auditLogService.log(username, "QUERY", "Promotion", promo.getId(), details);
        return PromotionMapper.toDto(promo);
    }

    @Override
    public PromotionDto updatePromotion(Long id, PromotionDto dto) {
        Promotion promo = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", id));

        StringBuilder changes = new StringBuilder("Updated promotion ID: ").append(id).append(". Changes: ");

        if (dto.getName() != null && !dto.getName().isBlank() && !dto.getName().equals(promo.getName())) {
            changes.append("name: ").append(promo.getName()).append(" → ").append(dto.getName()).append("; ");
            promo.setName(dto.getName());
        }

        if (dto.getDiscountPercent() != null && !dto.getDiscountPercent().equals(promo.getDiscountPercent())) {
            if (dto.getDiscountPercent() < 0 || dto.getDiscountPercent() > 100) {
                throw new PromotionException.InvalidDiscountRangeException();
            }
            changes.append("discountPercent: ").append(promo.getDiscountPercent()).append(" → ").append(dto.getDiscountPercent()).append("; ");
            promo.setDiscountPercent(dto.getDiscountPercent());
        }

        if (dto.getStartDate() != null && !dto.getStartDate().equals(promo.getStartDate())) {
            changes.append("startDate: ").append(promo.getStartDate()).append(" → ").append(dto.getStartDate()).append("; ");
            promo.setStartDate(dto.getStartDate());
        }

        if (dto.getEndDate() != null && !dto.getEndDate().equals(promo.getEndDate())) {
            changes.append("endDate: ").append(promo.getEndDate()).append(" → ").append(dto.getEndDate()).append("; ");
            promo.setEndDate(dto.getEndDate());
        }

        if (promo.getStartDate() != null && promo.getEndDate() != null) {
            if (!promo.getEndDate().isAfter(promo.getStartDate())) {
                throw new PromotionException.InvalidDateRangeException();
            }
        }

        Promotion updated = promotionRepository.save(promo);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log(username, "UPDATE", "Promotion", updated.getId(), changes.toString());

        return PromotionMapper.toDto(updated);
    }

    @Override
    public void deletePromotion(Long id) {
        Promotion promo = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", id));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String details = "Deleted promotion: [ID=" + promo.getId() +
                ", Name=" + promo.getName() +
                ", Discount=" + promo.getDiscountPercent() + "%" +
                ", Start=" + promo.getStartDate() +
                ", End=" + promo.getEndDate() + "]";

        auditLogService.log(username, "DELETE", "Promotion", promo.getId(), details);
        promotionRepository.delete(promo);
    }

    @Override
    public Page<PromotionDto> searchPromotions(PromotionFilterRequest request, int page, int size) {
        List<Promotion> promotions = promotionRepository.findAll();
        Stream<Promotion> stream = promotions.stream();

        if (request.getName() != null && !request.getName().isBlank()) {
            stream = stream.filter(p -> p.getName() != null &&
                    p.getName().toLowerCase().contains(request.getName().toLowerCase()));
        }

        if (request.getMinDiscount() != null) {
            stream = stream.filter(p -> p.getDiscountPercent() != null &&
                    p.getDiscountPercent() >= request.getMinDiscount());
        }

        if (request.getMaxDiscount() != null) {
            stream = stream.filter(p -> p.getDiscountPercent() != null &&
                    p.getDiscountPercent() <= request.getMaxDiscount());
        }

        if (request.getStartDateFrom() != null) {
            stream = stream.filter(p -> p.getStartDate() != null &&
                    !p.getStartDate().isBefore(request.getStartDateFrom()));
        }

        if (request.getStartDateTo() != null) {
            stream = stream.filter(p -> p.getStartDate() != null &&
                    !p.getStartDate().isAfter(request.getStartDateTo()));
        }

        if (request.getEndDateFrom() != null) {
            stream = stream.filter(p -> p.getEndDate() != null &&
                    !p.getEndDate().isBefore(request.getEndDateFrom()));
        }

        if (request.getEndDateTo() != null) {
            stream = stream.filter(p -> p.getEndDate() != null &&
                    !p.getEndDate().isAfter(request.getEndDateTo()));
        }

        List<Promotion> filteredPromotions = stream
                .sorted(Comparator.comparing(Promotion::getId).reversed())
                .toList();

        Pageable pageable = PageRequest.of(page, size);
        int start = Math.min((int) pageable.getOffset(), filteredPromotions.size());
        int end = Math.min(start + pageable.getPageSize(), filteredPromotions.size());

        List<Promotion> pagedPromotions = filteredPromotions.subList(start, end);

        Page<Promotion> promotionPage = new PageImpl<>(pagedPromotions, pageable, filteredPromotions.size());
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        StringBuilder details = new StringBuilder("Searched promotions with filters: ");

        if (request.getName() != null) {
            details.append("name=").append(request.getName()).append("; ");
        }
        if (request.getMinDiscount() != null) {
            details.append("minDiscount=").append(request.getMinDiscount()).append("; ");
        }
        if (request.getMaxDiscount() != null) {
            details.append("maxDiscount=").append(request.getMaxDiscount()).append("; ");
        }
        if (request.getStartDateFrom() != null) {
            details.append("startDateFrom=").append(request.getStartDateFrom()).append("; ");
        }
        if (request.getStartDateTo() != null) {
            details.append("startDateTo=").append(request.getStartDateTo()).append("; ");
        }
        if (request.getEndDateFrom() != null) {
            details.append("endDateFrom=").append(request.getEndDateFrom()).append("; ");
        }
        if (request.getEndDateTo() != null) {
            details.append("endDateTo=").append(request.getEndDateTo()).append("; ");
        }

        details.append("Page=").append(page)
                .append(", Size=").append(size)
                .append(", TotalResults=").append(filteredPromotions.size());

        auditLogService.log(username, "FILTER", "Promotion", null, details.toString());
        return promotionPage.map(PromotionMapper::toDto);
    }
}

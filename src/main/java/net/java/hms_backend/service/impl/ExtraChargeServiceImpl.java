package net.java.hms_backend.service.impl;

import net.java.hms_backend.dto.ExtraChargeDto;
import net.java.hms_backend.entity.ExtraCharge;
import net.java.hms_backend.exception.ResourceNotFoundException;
import net.java.hms_backend.mapper.ExtraChargeMapper;
import net.java.hms_backend.repository.ExtraChargeRepository;
import net.java.hms_backend.service.AuditLogService;
import net.java.hms_backend.service.ExtraChargeService;
import net.java.hms_backend.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ExtraChargeServiceImpl implements ExtraChargeService {

    private final ExtraChargeRepository repository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    public ExtraChargeServiceImpl(ExtraChargeRepository repository, AuditLogService auditLogService, NotificationService notificationService) {
        this.repository = repository;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
    }

    @Override
    public ExtraChargeDto create(ExtraChargeDto dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        LocalDateTime now = LocalDateTime.now();

        ExtraCharge entity = ExtraChargeMapper.toEntity(dto);
        entity.setCreatedBy(username);
        entity.setCreatedAt(now);

        ExtraCharge saved = repository.save(entity);

        String details = "Created extra charge: [ID=" + saved.getId() +
                ", Name=" + saved.getName() +
                ", Type=" + saved.getType() +
                ", Price=" + saved.getPrice() +
                ", Description=" + saved.getDescription() +
                ", CreatedBy=" + username +
                ", CreatedAt=" + now + "]";

        auditLogService.log(
                username,
                "CREATE",
                "ExtraCharge",
                saved.getId(),
                details
        );

        return ExtraChargeMapper.toDto(saved);
    }

    @Override
    public Page<ExtraChargeDto> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ExtraChargeDto> result = repository.findAll(pageable).map(ExtraChargeMapper::toDto);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log(
                username,
                "READ",
                "ExtraCharge",
                null,
                "Viewed paginated list of extra charges. Page: " + page + ", Size: " + size
        );

        return result;
    }

    @Override
    public ExtraChargeDto getById(Long id) {
        ExtraCharge entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExtraCharge", "id", id));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log(
                username,
                "READ",
                "ExtraCharge",
                id,
                "Viewed extra charge details: [ID=" + entity.getId() +
                        ", Name=" + entity.getName() +
                        ", Type=" + entity.getType() +
                        ", Price=" + entity.getPrice() +
                        ", Description=" + entity.getDescription() + "]"
        );

        return ExtraChargeMapper.toDto(entity);
    }

    @Override
    public void delete(Long id) {
        ExtraCharge entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExtraCharge", "id", id));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        String title = "Extra Charge Deleted";
        String message = "User " + username + " deleted extra charge: [ID=" + entity.getId() +
                ", Name=" + entity.getName() +
                ", Type=" + entity.getType() +
                ", Price=" + entity.getPrice() +
                ", Description=" + entity.getDescription() + "]";

        notificationService.notifyAdminsAndManagers(
                "EXTRA_CHARGE_DELETED",
                title,
                message
        );

        repository.deleteById(id);
    }

    @Override
    public ExtraChargeDto update(Long id, ExtraChargeDto dto) {
        ExtraCharge existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExtraCharge", "id", id));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        LocalDateTime now = LocalDateTime.now();

        StringBuilder changes = new StringBuilder("Updated ExtraCharge ID: " + existing.getId() + ". Changes: ");
        boolean hasChanges = false;

        if (dto.getName() != null && !dto.getName().equals(existing.getName())) {
            changes.append("name: ").append(existing.getName()).append(" → ").append(dto.getName()).append("; ");
            existing.setName(dto.getName());
            hasChanges = true;
        }

        if (dto.getType() != null && !dto.getType().equals(existing.getType())) {
            changes.append("type: ").append(existing.getType()).append(" → ").append(dto.getType()).append("; ");
            existing.setType(dto.getType());
            hasChanges = true;
        }

        if (dto.getPrice() != null && !dto.getPrice().equals(existing.getPrice())) {
            changes.append("price: ").append(existing.getPrice()).append(" → ").append(dto.getPrice()).append("; ");
            existing.setPrice(dto.getPrice());
            hasChanges = true;
        }

        if (dto.getDescription() != null && !dto.getDescription().equals(existing.getDescription())) {
            changes.append("description: ").append(existing.getDescription()).append(" → ").append(dto.getDescription()).append("; ");
            existing.setDescription(dto.getDescription());
            hasChanges = true;
        }

        if (!hasChanges) {
            return ExtraChargeMapper.toDto(existing);
        }

        existing.setUpdatedBy(username);
        existing.setUpdatedAt(now);

        ExtraCharge saved = repository.save(existing);

        auditLogService.log(
                username,
                "UPDATE",
                "ExtraCharge",
                saved.getId(),
                changes.toString()
        );

        return ExtraChargeMapper.toDto(saved);
    }
}

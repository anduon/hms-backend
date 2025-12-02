package net.java.hms_backend.service;

import net.java.hms_backend.dto.ExtraChargeDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ExtraChargeService {
    ExtraChargeDto create(ExtraChargeDto dto);
    Page<ExtraChargeDto> getAll(int page, int size);
    ExtraChargeDto getById(Long id);
    void delete(Long id);
    ExtraChargeDto update(Long id, ExtraChargeDto dto);
}


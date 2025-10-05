package net.java.hms_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.java.hms_backend.dto.ExtraChargeDto;
import net.java.hms_backend.service.ExtraChargeService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/extra-charges")
@RequiredArgsConstructor
public class ExtraChargeController {

    private final ExtraChargeService extraChargeService;

    @PostMapping
    public ResponseEntity<ExtraChargeDto> create(@Valid @RequestBody ExtraChargeDto dto) {
        return ResponseEntity.ok(extraChargeService.create(dto));
    }

    @GetMapping
    public ResponseEntity<Page<ExtraChargeDto>> getAll(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(extraChargeService.getAll(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExtraChargeDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(extraChargeService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExtraChargeDto> update(@PathVariable Long id, @RequestBody ExtraChargeDto dto) {
        return ResponseEntity.ok(extraChargeService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        extraChargeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}


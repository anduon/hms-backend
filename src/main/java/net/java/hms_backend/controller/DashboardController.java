package net.java.hms_backend.controller;

import lombok.AllArgsConstructor;
import net.java.hms_backend.dto.DashboardDto;
import net.java.hms_backend.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@AllArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardDto> getDashboardSummary() {
        DashboardDto summary = dashboardService.getDashboardSummary();
        return ResponseEntity.ok(summary);
    }
}
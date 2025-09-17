package net.java.hms_backend.service;

import net.java.hms_backend.dto.DashboardDto;

public interface DashboardService {
    DashboardDto getDashboardSummary(int days);
}

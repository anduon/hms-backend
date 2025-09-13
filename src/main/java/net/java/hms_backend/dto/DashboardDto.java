package net.java.hms_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {

    private Long totalBookings;
    private Long activeBookings;
    private Long checkedInBookings;
    private Long checkedOutBookingsToday;
    private Long cancelledBookings;
    private Long upcomingCheckInsToday;
    private Long upcomingCheckOutsToday;
    private Long totalGuestsCurrentMonth;


    private Long totalInvoices;
    private Long paidInvoices;
    private Long pendingInvoices;
    private BigDecimal totalRevenueGenerated;
    private BigDecimal totalOutstandingAmount;

    private Map<LocalDate, Long> bookingsPerDayLast7Days;
    private Map<String, Long> bookingsPerStatus;
    private Map<String, Long> invoicesPerStatus;
    private Map<String, BigDecimal> revenuePerPaymentMethod;
    private Map<LocalDate, BigDecimal> dailyRevenueLast7Days;
}
package net.java.hms_backend.service.impl;

import lombok.AllArgsConstructor;
import net.java.hms_backend.dto.DashboardDto;
import net.java.hms_backend.entity.Booking;
import net.java.hms_backend.entity.Invoice;
import net.java.hms_backend.repository.BookingRepository;
import net.java.hms_backend.repository.InvoiceRepository;
import net.java.hms_backend.service.DashboardService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final BookingRepository bookingRepository;
    private final InvoiceRepository invoiceRepository;

    @Override
    public DashboardDto getDashboardSummary() {
        DashboardDto dashboardDto = new DashboardDto();

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        dashboardDto.setTotalBookings(bookingRepository.count());
        dashboardDto.setCancelledBookings(bookingRepository.countByStatus("CANCELLED"));

        dashboardDto.setActiveBookings((long) bookingRepository.findByStatusAndCheckOutDateAfter("CONFIRMED", now).size());

        dashboardDto.setCheckedInBookings((long) bookingRepository.findByActualCheckInTimeIsNotNull().size());

        dashboardDto.setCheckedOutBookingsToday((long) bookingRepository.findByActualCheckOutTimeBetween(startOfDay, endOfDay).size());

        dashboardDto.setUpcomingCheckInsToday((long) bookingRepository.findByCheckInDateBetween(startOfDay, endOfDay).size());

        dashboardDto.setUpcomingCheckOutsToday((long) bookingRepository.findByCheckOutDateBetween(startOfDay, endOfDay).size());

        LocalDateTime firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
        LocalDateTime lastDayOfMonth = today.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);
        dashboardDto.setTotalGuestsCurrentMonth(bookingRepository.countTotalGuestsBetween(firstDayOfMonth, lastDayOfMonth));


        dashboardDto.setTotalInvoices(invoiceRepository.count());
        dashboardDto.setPaidInvoices(invoiceRepository.countByStatus("PAID"));
        dashboardDto.setPendingInvoices(invoiceRepository.countByStatus("PENDING"));

        BigDecimal totalRevenue = invoiceRepository.sumPaidAmountByStatus("PAID");
        dashboardDto.setTotalRevenueGenerated(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        BigDecimal outstandingAmount = invoiceRepository.sumOutstandingAmountByStatus("PENDING");
        dashboardDto.setTotalOutstandingAmount(outstandingAmount != null ? outstandingAmount : BigDecimal.ZERO);


        LocalDateTime sevenDaysAgo = startOfDay.minusDays(6);
        List<Booking> recentBookings = bookingRepository.findByCheckInDateAfter(sevenDaysAgo);
        Map<LocalDate, Long> bookingsPerDayMap = recentBookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getCheckInDate().toLocalDate(), Collectors.counting()));
        dashboardDto.setBookingsPerDayLast7Days(populateMissingDates(bookingsPerDayMap, today.minusDays(6), today));

        Map<String, Long> bookingsPerStatusMap = bookingRepository.findAll().stream()
                .collect(Collectors.groupingBy(Booking::getStatus, Collectors.counting()));
        dashboardDto.setBookingsPerStatus(bookingsPerStatusMap);

        Map<String, Long> invoicesPerStatusMap = invoiceRepository.findAll().stream()
                .collect(Collectors.groupingBy(Invoice::getStatus, Collectors.counting()));
        dashboardDto.setInvoicesPerStatus(invoicesPerStatusMap);

        Map<String, BigDecimal> revenuePerPaymentMethodMap = invoiceRepository.findByStatus("PAID").stream()
                .collect(Collectors.groupingBy(Invoice::getPaymentMethod,
                        Collectors.reducing(BigDecimal.ZERO, Invoice::getPaidAmount, BigDecimal::add)));
        dashboardDto.setRevenuePerPaymentMethod(revenuePerPaymentMethodMap);

        List<Invoice> recentPaidInvoices = invoiceRepository.findByIssuedDateAfter(sevenDaysAgo);
        Map<LocalDate, BigDecimal> dailyRevenueMap = recentPaidInvoices.stream()
                .filter(invoice -> "PAID".equals(invoice.getStatus()))
                .collect(Collectors.groupingBy(invoice -> invoice.getIssuedDate().toLocalDate(),
                        Collectors.reducing(BigDecimal.ZERO, Invoice::getPaidAmount, BigDecimal::add)));
        dashboardDto.setDailyRevenueLast7Days(populateMissingRevenueDates(dailyRevenueMap, today.minusDays(6), today));

        return dashboardDto;
    }

    private Map<LocalDate, Long> populateMissingDates(Map<LocalDate, Long> data, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, Long> populatedData = new LinkedHashMap<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            populatedData.put(date, data.getOrDefault(date, 0L));
        }
        return populatedData;
    }

    private Map<LocalDate, BigDecimal> populateMissingRevenueDates(Map<LocalDate, BigDecimal> data, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, BigDecimal> populatedData = new LinkedHashMap<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            populatedData.put(date, data.getOrDefault(date, BigDecimal.ZERO));
        }
        return populatedData;
    }
}
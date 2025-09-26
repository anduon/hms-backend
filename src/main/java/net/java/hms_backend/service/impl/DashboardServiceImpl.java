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
    public DashboardDto getDashboardSummary(int days) {
        DashboardDto dashboardDto = new DashboardDto();

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = today.atTime(LocalTime.MAX);

        dashboardDto.setTotalBookings(bookingRepository.count());
        dashboardDto.setCancelledBookings(bookingRepository.countByStatus("CANCELLED"));
        dashboardDto.setActiveBookings(
                (long) bookingRepository.findByStatusAndCheckOutDateAfter("CHECKED IN", endDateTime).size()
        );
        dashboardDto.setCheckedInBookings(
                (long) bookingRepository.findByActualCheckInTimeIsNotNull().size()
        );
        dashboardDto.setCheckedOutBookingsToday(
                (long) bookingRepository.findByActualCheckOutTimeBetween(startDateTime, endDateTime).size()
        );
        dashboardDto.setUpcomingCheckInsToday(
                (long) bookingRepository.findByCheckInDateBetween(startDateTime, endDateTime).size()
        );
        dashboardDto.setUpcomingCheckOutsToday(
                (long) bookingRepository.findByCheckOutDateBetween(startDateTime, endDateTime).size()
        );

        LocalDateTime firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
        LocalDateTime lastDayOfMonth = today.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);
        dashboardDto.setTotalGuestsCurrentMonth(
                bookingRepository.countTotalGuestsBetween(firstDayOfMonth, lastDayOfMonth)
        );

        dashboardDto.setTotalInvoices(invoiceRepository.count());
        dashboardDto.setPaidInvoices(invoiceRepository.countByStatus("PAID"));
        dashboardDto.setPendingInvoices(invoiceRepository.countByStatus("PENDING"));

        BigDecimal totalRevenue = invoiceRepository.sumPaidAmountByStatus("PAID");
        dashboardDto.setTotalRevenueGenerated(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        BigDecimal outstandingAmount = invoiceRepository.sumOutstandingAmountByStatus("PENDING");
        dashboardDto.setTotalOutstandingAmount(outstandingAmount != null ? outstandingAmount : BigDecimal.ZERO);

        List<Booking> recentBookings = bookingRepository.findByCheckInDateBetween(startDateTime, endDateTime);
        Map<LocalDate, Long> bookingsPerDayMap = recentBookings.stream()
                .filter(b -> b.getCheckInDate() != null)
                .collect(Collectors.groupingBy(
                        b -> b.getCheckInDate().toLocalDate(),
                        Collectors.counting()
                ));
        dashboardDto.setBookingsPerDayLast7Days(
                populateMissingDates(bookingsPerDayMap, startDate, today)
        );

        Map<String, Long> bookingsPerStatusMap = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() != null)
                .collect(Collectors.groupingBy(
                        Booking::getStatus,
                        Collectors.counting()
                ));
        dashboardDto.setBookingsPerStatus(bookingsPerStatusMap);

        Map<String, Long> invoicesPerStatusMap = invoiceRepository.findAll().stream()
                .filter(i -> i.getStatus() != null)
                .collect(Collectors.groupingBy(
                        Invoice::getStatus,
                        Collectors.counting()
                ));
        dashboardDto.setInvoicesPerStatus(invoicesPerStatusMap);

        Map<String, BigDecimal> revenuePerPaymentMethodMap = invoiceRepository.findByStatus("PAID").stream()
                .filter(i -> i.getPaymentMethod() != null && i.getPaidAmount() != null)
                .collect(Collectors.groupingBy(
                        Invoice::getPaymentMethod,
                        Collectors.reducing(BigDecimal.ZERO, Invoice::getPaidAmount, BigDecimal::add)
                ));
        dashboardDto.setRevenuePerPaymentMethod(revenuePerPaymentMethodMap);

        List<Invoice> recentPaidInvoices = invoiceRepository.findByIssuedDateBetween(startDateTime, endDateTime);
        Map<LocalDate, BigDecimal> dailyRevenueMap = recentPaidInvoices.stream()
                .filter(i -> "PAID".equals(i.getStatus()))
                .filter(i -> i.getIssuedDate() != null && i.getPaidAmount() != null)
                .collect(Collectors.groupingBy(
                        i -> i.getIssuedDate().toLocalDate(),
                        Collectors.reducing(BigDecimal.ZERO, Invoice::getPaidAmount, BigDecimal::add)
                ));
        dashboardDto.setDailyRevenueLast7Days(
                populateMissingRevenueDates(dailyRevenueMap, startDate, today)
        );

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
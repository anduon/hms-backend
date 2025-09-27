package net.java.hms_backend.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import net.java.hms_backend.dto.HotelInfoDto;
import net.java.hms_backend.dto.InvoiceDto;
import net.java.hms_backend.dto.InvoiceFilterRequest;
import net.java.hms_backend.entity.*;
import net.java.hms_backend.exception.InvoiceException;
import net.java.hms_backend.exception.ResourceNotFoundException;
import net.java.hms_backend.mapper.InvoiceMapper;
import net.java.hms_backend.repository.BookingRepository;
import net.java.hms_backend.repository.InvoiceRepository;
import net.java.hms_backend.service.HotelInfoService;
import net.java.hms_backend.service.InvoiceService;
import net.java.hms_backend.service.PromotionService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import net.java.hms_backend.entity.Invoice;

@Service
@AllArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final BookingRepository bookingRepository;
    private final HotelInfoService hotelInfoService;
    private final PromotionService promotionService;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public InvoiceDto createInvoice(InvoiceDto invoiceDto) {
        Booking booking = bookingRepository.findById(invoiceDto.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", invoiceDto.getBookingId()));

        boolean invoiceExists = invoiceRepository.existsByBookingId(booking.getId());
        if (invoiceExists) {
            throw new InvoiceException.DuplicateBookingException(
                    "Invoice already exists for booking ID: " + booking.getId(), null);
        }

        Room room = booking.getRoom();
        PriceType bookingPriceType = PriceType.valueOf(booking.getBookingType());

        RoomPrice roomPrice = room.getPrices().stream()
                .filter(p -> p.getPriceType() == bookingPriceType)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("RoomPrice", "priceType", bookingPriceType));

        LocalDateTime start = booking.getCheckInDate();
        LocalDateTime end = booking.getCheckOutDate();

        long durationInMinutes = Duration.between(start, end).toMinutes();
        if (durationInMinutes <= 0) durationInMinutes = 60;

        Optional<Promotion> promotionOpt = promotionService.getPromotionForBooking(start, end);
        double basePrice = roomPrice.getBasePrice();
        if (promotionOpt.isPresent()) {
            double discountPercent = promotionOpt.get().getDiscountPercent();
            basePrice *= (1 - discountPercent / 100.0);
        }

        BigDecimal amount;
        if (bookingPriceType == PriceType.HOURLY) {
            double hours = Math.ceil(durationInMinutes / 60.0);
            amount = BigDecimal.valueOf(basePrice * hours);
        } else {
            long days = (long) Math.ceil(Duration.between(start, end).toMinutes() / 1440.0);
            if (days <= 0) days = 1;
            amount = BigDecimal.valueOf(basePrice * days);
        }

        amount = amount.setScale(0, RoundingMode.HALF_UP);
        Invoice invoice = InvoiceMapper.mapToInvoice(invoiceDto, booking);
        invoice.setAmount(amount);
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setIssuedDate(LocalDateTime.now());

        Invoice savedInvoice = invoiceRepository.save(invoice);

        return InvoiceMapper.mapToInvoiceDto(savedInvoice);
    }

    @Override
    public InvoiceDto getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
        return InvoiceMapper.mapToInvoiceDto(invoice);
    }

    @Override
    public Page<InvoiceDto> getAllInvoices(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Invoice> invoicesPage = invoiceRepository.findAll(pageable);
        return invoicesPage.map(InvoiceMapper::mapToInvoiceDto);
    }


    @Override
    public InvoiceDto updateInvoice(Long id, InvoiceDto invoiceDto) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));

        if (invoiceDto.getBookingId() != null &&
                (invoice.getBooking() == null || !invoiceDto.getBookingId().equals(invoice.getBooking().getId()))) {

            Booking booking = bookingRepository.findById(invoiceDto.getBookingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", invoiceDto.getBookingId()));
            invoice.setBooking(booking);

            Room room = booking.getRoom();
            PriceType bookingPriceType = PriceType.valueOf(booking.getBookingType());

            RoomPrice roomPrice = room.getPrices().stream()
                    .filter(p -> p.getPriceType() == bookingPriceType)
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("RoomPrice", "priceType", bookingPriceType));

            LocalDateTime start = booking.getCheckInDate();
            LocalDateTime end = booking.getCheckOutDate();
            long durationInMinutes = Duration.between(start, end).toMinutes();
            if (durationInMinutes <= 0) durationInMinutes = 60;

            Optional<Promotion> promotionOpt = promotionService.getPromotionForBooking(start, end);

            double basePrice = roomPrice.getBasePrice();
            if (promotionOpt.isPresent()) {
                double discountPercent = promotionOpt.get().getDiscountPercent();
                basePrice *= (1 - discountPercent / 100.0);
            }

            BigDecimal amount;
            if (bookingPriceType == PriceType.HOURLY) {
                double hours = Math.ceil(durationInMinutes / 60.0);
                amount = BigDecimal.valueOf(basePrice * hours);
            } else {
                long days = (long) Math.ceil(Duration.between(start, end).toMinutes() / 1440.0);
                if (days <= 0) days = 1;
                amount = BigDecimal.valueOf(basePrice * days);
            }

            amount = amount.setScale(0, RoundingMode.HALF_UP);
            invoice.setAmount(amount);
        }

        if (invoiceDto.getPaidAmount() != null) {
            invoice.setPaidAmount(invoiceDto.getPaidAmount());
        }
        if (invoiceDto.getStatus() != null) {
            invoice.setStatus(invoiceDto.getStatus());
        }
        if (invoiceDto.getIssuedDate() != null) {
            invoice.setIssuedDate(invoiceDto.getIssuedDate());
        }
        if (invoiceDto.getDueDate() != null) {
            invoice.setDueDate(invoiceDto.getDueDate());
        }
        if (invoiceDto.getPaymentMethod() != null) {
            invoice.setPaymentMethod(invoiceDto.getPaymentMethod());
        }
        if (invoiceDto.getNotes() != null) {
            invoice.setNotes(invoiceDto.getNotes());
        }

        Invoice updatedInvoice = invoiceRepository.save(invoice);
        return InvoiceMapper.mapToInvoiceDto(updatedInvoice);
    }



    @Override
    public void deleteInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
        invoiceRepository.delete(invoice);
    }

    @Override
    public Page<InvoiceDto> filterInvoices(InvoiceFilterRequest filter, int page, int size) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Invoice> query = cb.createQuery(Invoice.class);
        Root<Invoice> invoice = query.from(Invoice.class);
        query.select(invoice).distinct(true);

        List<Predicate> predicates = buildPredicates(filter, cb, invoice);
        query.where(cb.and(predicates.toArray(new Predicate[0])));

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        List<Invoice> result = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Invoice> countRoot = countQuery.from(Invoice.class);
        List<Predicate> countPredicates = buildPredicates(filter, cb, countRoot);

        countQuery.select(cb.countDistinct(countRoot));
        countQuery.where(cb.and(countPredicates.toArray(new Predicate[0])));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        Page<Invoice> invoicePage = new PageImpl<>(result, pageable, total);
        return invoicePage.map(InvoiceMapper::mapToInvoiceDto);
    }

    private List<Predicate> buildPredicates(InvoiceFilterRequest filter, CriteriaBuilder cb, Root<Invoice> root) {
        List<Predicate> predicates = new ArrayList<>();

        if (filter.getMinAmount() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), filter.getMinAmount()));
        }

        if (filter.getMaxAmount() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("amount"), filter.getMaxAmount()));
        }

        if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
            predicates.add(cb.equal(root.get("status"), filter.getStatus()));
        }

        if (filter.getIssuedDateFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("issuedDate"), filter.getIssuedDateFrom()));
        }

        if (filter.getIssuedDateTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("issuedDate"), filter.getIssuedDateTo()));
        }

        if (filter.getDueDateFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("dueDate"), filter.getDueDateFrom()));
        }

        if (filter.getDueDateTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("dueDate"), filter.getDueDateTo()));
        }

        if (filter.getPaymentMethod() != null && !filter.getPaymentMethod().isBlank()) {
            predicates.add(cb.equal(root.get("paymentMethod"), filter.getPaymentMethod()));
        }

        if (filter.getBookingId() != null) {
            predicates.add(cb.equal(root.get("booking").get("id"), filter.getBookingId()));
        }

        return predicates;
    }

    @Override
    public byte[] generateInvoicePdf(InvoiceDto invoiceDto) {
        try {
            HotelInfoDto hotel = hotelInfoService.getHotelInfo();

            Booking booking = bookingRepository.findById(invoiceDto.getBookingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", invoiceDto.getBookingId()));
            Invoice invoice = InvoiceMapper.mapToInvoice(invoiceDto, booking);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter.getInstance(document, out);

            document.open();

            Font hotelFont = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.BOLD);
            document.add(new Paragraph(hotel.getName(), hotelFont));
            document.add(new Paragraph(hotel.getAddress()));
            document.add(new Paragraph("Phone: " + hotel.getPhone() + " | Email: " + hotel.getEmail()));
            document.add(new Paragraph("MST: " + hotel.getTaxCode()));
            document.add(new Paragraph(" "));

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA, 20, Font.BOLD);
            Paragraph title = new Paragraph("HOA DON THANH TOAN (INVOICE)", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Số hóa đơn: " + invoice.getId()));
            document.add(new Paragraph("Mã Booking: " + booking.getId()));
            document.add(new Paragraph("Khách hàng: " + booking.getGuestFullName()));
            document.add(new Paragraph("Số phòng: " + booking.getRoom().getRoomNumber()));
            document.add(new Paragraph("Ngày lập: " + invoice.getIssuedDate()));
            document.add(new Paragraph("Hạn thanh toán: " + invoice.getDueDate()));
            document.add(new Paragraph("Trạng thái: " + invoice.getStatus()));
            document.add(new Paragraph(" "));

            PdfPTable serviceTable = new PdfPTable(4);
            serviceTable.setWidthPercentage(100);
            serviceTable.setWidths(new int[]{4, 1, 2, 2});

            serviceTable.addCell(new PdfPCell(new Phrase("Dịch vụ")));
            serviceTable.addCell(new PdfPCell(new Phrase("SL")));
            serviceTable.addCell(new PdfPCell(new Phrase("Đơn giá")));
            serviceTable.addCell(new PdfPCell(new Phrase("Thành tiền")));

            Room room = booking.getRoom();
            PriceType bookingPriceType = PriceType.valueOf(booking.getBookingType());

            RoomPrice roomPrice = room.getPrices().stream()
                    .filter(p -> p.getPriceType() == bookingPriceType)
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("RoomPrice", "priceType", bookingPriceType));

            LocalDateTime start = booking.getActualCheckInTime() != null ? booking.getActualCheckInTime() : booking.getCheckInDate();
            LocalDateTime end = booking.getActualCheckOutTime() != null ? booking.getActualCheckOutTime() : booking.getCheckOutDate();

            long nights = java.time.Duration.between(start, end).toDays();
            BigDecimal amount = BigDecimal.valueOf(roomPrice.getBasePrice() * nights);
            serviceTable.addCell("Tiền phòng (" + nights + " đêm)");
            serviceTable.addCell(String.valueOf(nights));
            serviceTable.addCell(String.valueOf(roomPrice.getBasePrice()));
            serviceTable.addCell(String.valueOf(amount));

            document.add(serviceTable);
            document.add(new Paragraph(" "));

            BigDecimal subTotal = invoice.getAmount();
            BigDecimal vat = subTotal.multiply(BigDecimal.valueOf(0.1));
            BigDecimal grandTotal = subTotal.add(vat);

            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(50);
            summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            summaryTable.addCell("Tổng tiền (chưa VAT)");
            summaryTable.addCell(subTotal.toPlainString());

            summaryTable.addCell("VAT (10%)");
            summaryTable.addCell(vat.toPlainString());

            summaryTable.addCell("TỔNG THANH TOÁN");
            summaryTable.addCell(grandTotal.toPlainString());

            summaryTable.addCell("Đã thanh toán");
            summaryTable.addCell(invoice.getPaidAmount().toPlainString());

            summaryTable.addCell("Còn lại");
            summaryTable.addCell(grandTotal.subtract(invoice.getPaidAmount()).toPlainString());

            document.add(summaryTable);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Phương thức thanh toán: " + (invoice.getPaymentMethod() != null ? invoice.getPaymentMethod() : "Chưa xác định")));
            document.add(new Paragraph("Ghi chú: " + (invoice.getNotes() != null ? invoice.getNotes() : "")));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Xin cam on Quy khach da su dung dich vu cua chung toi!", hotelFont));

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new InvoiceException.PdfGenerationException("Error while generating PDF", e);
        }
    }
}

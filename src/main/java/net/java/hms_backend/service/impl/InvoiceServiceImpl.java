package net.java.hms_backend.service.impl;

import lombok.AllArgsConstructor;
import net.java.hms_backend.dto.InvoiceDto;
import net.java.hms_backend.entity.*;
import net.java.hms_backend.exception.ResourceNotFoundException;
import net.java.hms_backend.mapper.InvoiceMapper;
import net.java.hms_backend.repository.BookingRepository;
import net.java.hms_backend.repository.InvoiceRepository;
import net.java.hms_backend.service.InvoiceService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import net.java.hms_backend.entity.Invoice;

@Service
@AllArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final BookingRepository bookingRepository;

    @Override
    public InvoiceDto createInvoice(InvoiceDto invoiceDto) {
        Booking booking = bookingRepository.findById(invoiceDto.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", invoiceDto.getBookingId()));

        Room room = booking.getRoom();
        PriceType bookingPriceType = PriceType.valueOf(booking.getBookingType());

        RoomPrice roomPrice = room.getPrices().stream()
                .filter(p -> p.getPriceType() == bookingPriceType)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("RoomPrice", "priceType", bookingPriceType));

        LocalDateTime start = booking.getActualCheckInTime() != null ? booking.getActualCheckInTime() : booking.getCheckInDate();
        LocalDateTime end = booking.getActualCheckOutTime() != null ? booking.getActualCheckOutTime() : booking.getCheckOutDate();

        long days = java.time.Duration.between(start, end).toDays();
        if (days <= 0) days = 1;

        BigDecimal amount = BigDecimal.valueOf(roomPrice.getBasePrice() * days);

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
    public List<InvoiceDto> getAllInvoices() {
        return invoiceRepository.findAll()
                .stream()
                .map(InvoiceMapper::mapToInvoiceDto)
                .collect(Collectors.toList());
    }

    @Override
    public InvoiceDto updateInvoice(Long id, InvoiceDto invoiceDto) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));

        Booking booking = bookingRepository.findById(invoiceDto.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", invoiceDto.getBookingId()));

        invoice.setAmount(invoiceDto.getAmount());
        invoice.setPaidAmount(invoiceDto.getPaidAmount());
        invoice.setStatus(invoiceDto.getStatus());
        invoice.setIssuedDate(invoiceDto.getIssuedDate());
        invoice.setDueDate(invoiceDto.getDueDate());
        invoice.setPaymentMethod(invoiceDto.getPaymentMethod());
        invoice.setNotes(invoiceDto.getNotes());
        invoice.setBooking(booking);

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
    public byte[] generateInvoicePdf(InvoiceDto invoiceDto) {
        try {
            Booking booking = bookingRepository.findById(invoiceDto.getBookingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", invoiceDto.getBookingId()));
            Invoice invoice = InvoiceMapper.mapToInvoice(invoiceDto, booking);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter.getInstance(document, out);

            document.open();

            Font hotelFont = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.BOLD);
            document.add(new Paragraph("HOTELIO", hotelFont));
            document.add(new Paragraph("123 Đường ABC, Quận 1, TP.HCM"));
            document.add(new Paragraph("Phone: 0123-456-789 | Email: info@sunshinehotel.com"));
            document.add(new Paragraph("MST: 123456789"));
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

            // ====== PAYMENT METHOD & NOTES ======
            document.add(new Paragraph("Phương thức thanh toán: " + (invoice.getPaymentMethod() != null ? invoice.getPaymentMethod() : "Chưa xác định")));
            document.add(new Paragraph("Ghi chú: " + (invoice.getNotes() != null ? invoice.getNotes() : "")));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Xin cam on Quy khach da su dung dich vu cua chung toi!", hotelFont));

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error while generating PDF: " + e.getMessage(), e);
        }
    }
}

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
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);

            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.BLACK);
            Paragraph title = new Paragraph("HOTEL INVOICE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Invoice ID: " + invoice.getId()));
            document.add(new Paragraph("Booking ID: " + invoice.getBooking().getId()));
            document.add(new Paragraph("Customer: " + invoice.getBooking().getGuestFullName()));
            document.add(new Paragraph("Room: " + invoice.getBooking().getRoom().getRoomNumber()));
            document.add(new Paragraph("Issued Date: " + invoice.getIssuedDate()));
            document.add(new Paragraph("Due Date: " + invoice.getDueDate()));
            document.add(new Paragraph("Status: " + invoice.getStatus()));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            PdfPCell header1 = new PdfPCell(new Phrase("Description"));
            PdfPCell header2 = new PdfPCell(new Phrase("Amount"));
            table.addCell(header1);
            table.addCell(header2);

            table.addCell("Room charges");
            table.addCell(invoice.getAmount().toPlainString());

            table.addCell("Paid");
            table.addCell(invoice.getPaidAmount().toPlainString());

            table.addCell("Remaining");
            table.addCell(invoice.getAmount().subtract(invoice.getPaidAmount()).toPlainString());

            document.add(table);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Notes: " + (invoice.getNotes() != null ? invoice.getNotes() : "")));

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error while generating PDF: " + e.getMessage(), e);
        }
    }
}

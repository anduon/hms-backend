package net.java.hms_backend.mapper;

import net.java.hms_backend.dto.InvoiceDto;
import net.java.hms_backend.entity.Booking;
import net.java.hms_backend.entity.Invoice;

public class InvoiceMapper {

    public static InvoiceDto mapToInvoiceDto(Invoice invoice) {
        return new InvoiceDto(
                invoice.getId(),
                invoice.getBooking().getId(),
                invoice.getAmount(),
                invoice.getPaidAmount(),
                invoice.getStatus(),
                invoice.getIssuedDate(),
                invoice.getDueDate(),
                invoice.getPaymentMethod(),
                invoice.getNotes()
        );
    }

    public static Invoice mapToInvoice(InvoiceDto dto, Booking booking) {
        return new Invoice(
                dto.getId(),
                dto.getAmount(),
                dto.getPaidAmount(),
                dto.getStatus(),
                dto.getIssuedDate(),
                dto.getDueDate(),
                dto.getPaymentMethod(),
                dto.getNotes(),
                booking
        );
    }
}

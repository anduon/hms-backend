package net.java.hms_backend.mapper;

import net.java.hms_backend.dto.InvoiceDto;
import net.java.hms_backend.entity.Booking;
import net.java.hms_backend.entity.Invoice;

public class InvoiceMapper {

    public static InvoiceDto mapToInvoiceDto(Invoice invoice) {
        InvoiceDto dto = new InvoiceDto();
        dto.setId(invoice.getId());
        dto.setBookingId(invoice.getBooking().getId());
        dto.setAmount(invoice.getAmount());
        dto.setPaidAmount(invoice.getPaidAmount());
        dto.setStatus(invoice.getStatus());
        dto.setIssuedDate(invoice.getIssuedDate());
        dto.setDueDate(invoice.getDueDate());
        dto.setPaymentMethod(invoice.getPaymentMethod());
        dto.setNotes(invoice.getNotes());
        BaseMapper.mapAuditFields(invoice, dto);

        return dto;
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

package net.java.hms_backend.service;

import net.java.hms_backend.dto.InvoiceDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface InvoiceService {
    InvoiceDto createInvoice(InvoiceDto dto);
    InvoiceDto getInvoiceById(Long id);
    Page<InvoiceDto> getAllInvoices(int page, int size);
    InvoiceDto updateInvoice(Long id, InvoiceDto dto);
    void deleteInvoice(Long id);
    byte[] generateInvoicePdf(InvoiceDto invoiceDto);
}

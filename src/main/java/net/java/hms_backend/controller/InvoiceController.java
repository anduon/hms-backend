package net.java.hms_backend.controller;

import lombok.AllArgsConstructor;
import net.java.hms_backend.dto.InvoiceDto;
import net.java.hms_backend.dto.InvoiceFilterRequest;
import net.java.hms_backend.service.InvoiceService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import org.springframework.http.HttpHeaders;

@AllArgsConstructor
@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private InvoiceService invoiceService;

    @PostMapping
    public ResponseEntity<InvoiceDto> createInvoice(@RequestBody InvoiceDto invoiceDto) {
        InvoiceDto savedInvoice = invoiceService.createInvoice(invoiceDto);
        return new ResponseEntity<>(savedInvoice, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    @GetMapping
    public ResponseEntity<Page<InvoiceDto>> getAllInvoices(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(invoiceService.getAllInvoices(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDto> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvoiceDto> updateInvoice(@PathVariable Long id, @RequestBody InvoiceDto invoiceDto) {
        return ResponseEntity.ok(invoiceService.updateInvoice(id, invoiceDto));
    }

    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.ok("Invoice deleted successfully.");
    }

    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    @PostMapping("/filter")
    public ResponseEntity<Page<InvoiceDto>> filterInvoices(
            @RequestBody InvoiceFilterRequest filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<InvoiceDto> invoices = invoiceService.filterInvoices(filter, page, size);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("{id}/pdf")
    public ResponseEntity<byte[]> generateInvoicePdf(@PathVariable Long id) throws IOException {
        InvoiceDto invoiceDto = invoiceService.getInvoiceById(id);

        byte[] pdfBytes = invoiceService.generateInvoicePdf(invoiceDto);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_" + id + ".pdf");
        headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(pdfBytes);
    }


}

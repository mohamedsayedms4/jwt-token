package org.example.mobilyecommerce.controller;

import org.example.mobilyecommerce.model.SupplierInvoice;
import org.example.mobilyecommerce.service.impl.SupplierInvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class SupplierInvoiceController {

    private final SupplierInvoiceService invoiceService;

    public SupplierInvoiceController(SupplierInvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public List<SupplierInvoice> getAllInvoices() {
        return invoiceService.getAllInvoices();
    }

    @GetMapping("/supplier/{supplierId}")
    public List<SupplierInvoice> getInvoicesBySupplier(@PathVariable Long supplierId) {
        return invoiceService.getInvoicesBySupplier(supplierId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierInvoice> getInvoiceById(@PathVariable Long id) {
        return invoiceService.getInvoiceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public SupplierInvoice createInvoice(@RequestBody SupplierInvoice invoice) {
        return invoiceService.saveInvoice(invoice);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierInvoice> updateInvoice(@PathVariable Long id, @RequestBody SupplierInvoice invoiceDetails) {
        return invoiceService.getInvoiceById(id).map(invoice -> {
            invoice.setInvoiceDate(invoiceDetails.getInvoiceDate());
            invoice.setPaidAmount(invoiceDetails.getPaidAmount());
            invoice.setRemainingAmount(invoiceDetails.getRemainingAmount());
            invoice.setTotalAmount(invoiceDetails.getTotalAmount());
            invoice.setNotes(invoiceDetails.getNotes());
            invoice.setItems(invoiceDetails.getItems());
            invoice.setSupplier(invoiceDetails.getSupplier());
            return ResponseEntity.ok(invoiceService.saveInvoice(invoice));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }
}

package org.example.mobilyecommerce.service.impl;


import org.example.mobilyecommerce.model.SupplierInvoice;
import org.example.mobilyecommerce.repository.SupplierInvoiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SupplierInvoiceService {

    private final SupplierInvoiceRepository invoiceRepository;

    public SupplierInvoiceService(SupplierInvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public List<SupplierInvoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public List<SupplierInvoice> getInvoicesBySupplier(Long supplierId) {
        return invoiceRepository.findBySupplierId(supplierId);
    }

    public Optional<SupplierInvoice> getInvoiceById(Long id) {
        return invoiceRepository.findById(id);
    }

    public SupplierInvoice saveInvoice(SupplierInvoice invoice) {
        return invoiceRepository.save(invoice);
    }

    public void deleteInvoice(Long id) {
        invoiceRepository.deleteById(id);
    }
}

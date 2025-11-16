package org.example.mobilyecommerce.repository;

import org.example.mobilyecommerce.model.SupplierInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierInvoiceRepository extends JpaRepository<SupplierInvoice, Long> {
    List<SupplierInvoice> findBySupplierId(Long supplierId); // كل الفواتير لمورد معين
}

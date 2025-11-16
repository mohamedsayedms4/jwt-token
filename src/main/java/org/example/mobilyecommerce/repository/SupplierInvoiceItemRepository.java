package org.example.mobilyecommerce.repository;

import org.example.mobilyecommerce.model.SupplierInvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierInvoiceItemRepository extends JpaRepository<SupplierInvoiceItem, Long> {
}

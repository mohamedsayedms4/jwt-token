package org.example.mobilyecommerce.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * كلاس بيمثل المنتج أو البند داخل فاتورة المورد
 */
@Entity
@Table(name = "supplier_invoice_items")

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierInvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // رقم البند (المعرف الأساسي)

    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    @JsonBackReference
    private SupplierInvoice invoice;

    List<Long> productIds;
    private int quantity;       // الكمية المشتراة من المورد
    private double unitPrice;   // سعر الوحدة
    private double totalPrice;  // الإجمالي (الكمية × السعر)

}

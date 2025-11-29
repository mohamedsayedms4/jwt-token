package org.example.mobilyecommerce.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

/**
 * كلاس بيمثل فاتورة شراء من المورد
 */
@Entity
@Table(name = "supplier_invoices")
public class SupplierInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // رقم الفاتورة (المعرف الأساسي)


    private LocalDate invoiceDate; // تاريخ الفاتورة

    private double totalAmount;    // إجمالي قيمة الفاتورة
    private double paidAmount;     // المبلغ اللي اتدفع فعلاً للمورد
    private double remainingAmount;// المبلغ المتبقي اللي لسه ما اتدفعش

    private String notes;          // ملاحظات إضافية (زي رقم إيصال أو خصم)

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    @JsonIgnoreProperties("invoices")
    private Supplier supplier;


    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<SupplierInvoiceItem> items;
    // ----- Getters & Setters -----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }

    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(double paidAmount) { this.paidAmount = paidAmount; }

    public double getRemainingAmount() { return remainingAmount; }
    public void setRemainingAmount(double remainingAmount) { this.remainingAmount = remainingAmount; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<SupplierInvoiceItem> getItems() { return items; }
    public void setItems(List<SupplierInvoiceItem> items) { this.items = items; }
}

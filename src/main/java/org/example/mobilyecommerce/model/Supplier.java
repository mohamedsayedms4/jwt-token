package org.example.mobilyecommerce.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * كلاس بيمثل المورد اللي التاجر بيجيب منه البضاعة
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "suppliers")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                // رقم المورد (المعرف الأساسي)

    @Column(nullable = false, columnDefinition = "NVARCHAR(100)")
    private String name;            // اسم المورد

    @Column(nullable = false, columnDefinition = "NVARCHAR(50)")
    private String phone;           // رقم الهاتف للتواصل

    @Column(columnDefinition = "NVARCHAR(255)")
    private String telegramLink;    // لينك حساب التلجرام

    @Column(columnDefinition = "NVARCHAR(255)")
    private String whatsappLink;    // لينك الواتساب

    @Column(nullable = false)
    private double totalWithdraw = 0.0;   // إجمالي المبلغ اللي سحبه المورد (اللي اتدفع له)

    @Column(nullable = false)
    private double totalPaid = 0.0;       // إجمالي المبلغ اللي التاجر دفعه للمورد

    @Column(nullable = false)
    private double totalDue = 0.0;        // المبلغ المتبقي للمورد (لو فيه ديون أو فلوس لسه ما اتدفعتش)

    // العلاقة بين المورد والفواتير
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference   // هنا نحدد أن هذه الجهة هي الأصل
    private List<SupplierInvoice> invoices; // قائمة الفواتير الخاصة بالمورد
}

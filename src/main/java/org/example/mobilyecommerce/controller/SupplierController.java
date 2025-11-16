package org.example.mobilyecommerce.controller;

import org.example.mobilyecommerce.model.Supplier;
import org.example.mobilyecommerce.service.impl.SupplierService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public List<Supplier> getAllSuppliers() {
        return supplierService.getAllSuppliers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Supplier> getSupplierById(@PathVariable Long id) {
        return supplierService.getSupplierById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Supplier createSupplier(@RequestBody Supplier supplier) {
        return supplierService.saveSupplier(supplier);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Supplier> updateSupplier(@PathVariable Long id, @RequestBody Supplier supplierDetails) {
        return supplierService.getSupplierById(id).map(supplier -> {
            supplier.setName(supplierDetails.getName());
            supplier.setPhone(supplierDetails.getPhone());
            supplier.setTelegramLink(supplierDetails.getTelegramLink());
            supplier.setWhatsappLink(supplierDetails.getWhatsappLink());
            supplier.setTotalPaid(supplierDetails.getTotalPaid());
            supplier.setTotalWithdraw(supplierDetails.getTotalWithdraw());
            supplier.setTotalDue(supplierDetails.getTotalDue());
            return ResponseEntity.ok(supplierService.saveSupplier(supplier));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }
}

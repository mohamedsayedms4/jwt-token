package org.example.mobilyecommerce.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mobilyecommerce.dto.CategoryDto;
import org.example.mobilyecommerce.dto.ProductDto;
import org.example.mobilyecommerce.service.CategoryService;
import org.example.mobilyecommerce.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing product categories.
 * Supports CRUD operations and image upload for each category.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final ProductService productService;

    /**
     * 🟢 Create a new category with optional image upload.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> createCategory(
            @RequestPart("category") CategoryDto categoryDto,
            @RequestPart(value = "icon", required = false) MultipartFile icon) {

        Optional<CategoryDto> created = categoryService.createCategory(categoryDto, icon);
        return created.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to create category"));
    }

    /**
     * 🟡 Update existing category.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateCategory(
            @PathVariable Long id,
            @RequestPart("category") CategoryDto categoryDto,
            @RequestPart(value = "icon", required = false) MultipartFile icon) {

        Optional<CategoryDto> updated = categoryService.updateCategory(id, categoryDto, icon);
        return updated.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Category not found or update failed"));
    }

    /**
     * 🔴 Delete category by ID.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        Boolean deleted = categoryService.deleteCategory(id);
        if (deleted) {
            return ResponseEntity.ok("Category deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category not found");
        }
    }

    /**
     * 🔍 Get single category by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategory(@PathVariable Long id) {
        Optional<CategoryDto> category = categoryService.getCategory(id);
        return category.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category not found"));
    }

    /**
     * 📋 Get all categories (flat list).
     */
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/root")
    public ResponseEntity<List<CategoryDto>> getRootCategories() {
        return ResponseEntity.ok(categoryService.getRootCategories());
    }


    /**
     * 📦 Get all products by category ID
     * @param categoryId the category ID
     * @return list of ProductDto
     */
    @GetMapping("/{categoryId}/products")
    public ResponseEntity<?> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId, page, size));
    }

    @GetMapping("/{parentCategoryId}/all-products")
    public ResponseEntity<?> getProductsByParentCategory(
            @PathVariable Long parentCategoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(productService.getProductsByParentCategory(parentCategoryId, page, size));
    }

}

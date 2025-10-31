package org.example.mobilyecommerce.service;


import org.example.mobilyecommerce.dto.CategoryDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing categories in the e-commerce application.
 * Provides methods for creating, updating, deleting, and retrieving categories.
 */
public interface CategoryService {

    /**
     * Creates a new category with the given details and icon.
     * Access restricted to users with ROLE_ADMIN.
     *
     * @param categoryDto the DTO containing category data without icon
     * @param icon        the icon file for the category
     * @return Optional containing the created category DTO
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    Optional<CategoryDto> createCategory(CategoryDto categoryDto, MultipartFile icon);

    /**
     * Updates an existing category with new details and icon.
     * Access restricted to users with ROLE_ADMIN.
     *
     * @param id                the ID of the category to update
     * @param createCategoryDto the DTO containing updated category data
     * @param icon              the new icon file for the category
     * @return Optional containing the updated category DTO
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    Optional<CategoryDto> updateCategory(Long id, CategoryDto createCategoryDto, MultipartFile icon);

    /**
     * Deletes a category by its ID.
     * Access restricted to users with ROLE_ADMIN.
     *
     * @param id the ID of the category to delete
     * @return true if the category was deleted successfully
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    Boolean deleteCategory(Long id);

    /**
     * Retrieves a category by its ID.
     *
     * @param id the ID of the category
     * @return Optional containing the category if found
     */
    Optional<CategoryDto> getCategory(Long id);

    /**
     * Retrieves all categories in hierarchical structure.
     *
     * @return list of root categories with their children populated
     */
    List<CategoryDto> getAllCategories();
}

package org.example.mobilyecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object (DTO) for the {@link org.example.mobilyecommerce.model.Category} entity.
 * Used to transfer category data between the backend and frontend layers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    /**
     * Unique identifier of the category.
     */
    private Long id;

    /**
     * English name of the category.
     */
    private String nameEn;

    /**
     * Arabic name of the category.
     */
    private String nameAr;

    /**
     * Custom string identifier for external reference.
     */
    private String categoryId;

    /**
     * ID of the parent category (null if this is a root category).
     */
    private Long parentId;

    /**
     * Level of the category in the hierarchy.
     */
    private Integer level;

    /**
     * URL of the category image/icon.
     */
    private String imageUrl;

    /**
     * List of child categories (recursive structure).
     * This helps represent nested categories in a tree format.
     */
    private List<CategoryDto> children;
}

package org.example.mobilyecommerce.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a Category in the e-commerce system.
 * Supports hierarchical structure with parent and child categories.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category  {

    /**
     * Electronics (level 0)
     *  ├── Mobiles (level 1)
     *  │    └── Accessories (level 2)
     *  └── Laptops (level 1)
     *  *
    /**
     * English name of the category.
     * Must be unique and not null.
     */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;

    @Column(unique = true, nullable = false)
    private String nameEn;

    /**
     * Arabic name of the category.
     * Must be unique, not null, stored as NVARCHAR(100).
     */
    @Column(unique = true, nullable = false, columnDefinition = "NVARCHAR(100)")
    private String nameAr;

    /**
     * Custom string identifier for the category.
     * Must be unique and not null.
     */
    @NotNull
    @Column(unique = true)
    private String categoryId;

    /**
     * Reference to the parent category (for hierarchical structure).
     * Lazy-loaded.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    @JsonBackReference
    private Category parentCategory;

    /**
     * List of child categories.
     * Cascade all operations, and remove orphans automatically.
     */
    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> children = new ArrayList<>();

    /**
     * Level of the category in the hierarchy.
     * Must not be null.
     */
    @NotNull
    private Integer level;

    /**
     * URL of the category image/icon.
     */
    private String imageUrl;

}

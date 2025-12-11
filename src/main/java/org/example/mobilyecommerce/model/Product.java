package org.example.mobilyecommerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
@Entity
public class Product extends BaseEntity {
    @Column(nullable = false,columnDefinition= "NVARCHAR(100)")
    private String title;

    @Column(nullable = false,columnDefinition= "NVARCHAR(100)")
    private String description;

    private Long purchasPrice;

    private Long sellingPrice;

    @Min(0)
    @Max(100)
    @Column(nullable = true)
    private Integer discountPercentage;

    private Boolean isVerified = false;

    @Min(0)
    private Integer quantity;

    private String color;

    @ManyToOne(optional = false)
    private Category category;


//    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Review> reviews = new ArrayList<>();

    private Long viewsCounter = 0L;

    private Long searchCounter = 0L;


    @ElementCollection
    @CollectionTable(
            name = "product_images",
            joinColumns = @JoinColumn(name = "product_id")
    )
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "icons_id", referencedColumnName = "id")
    private Icons icons;


}

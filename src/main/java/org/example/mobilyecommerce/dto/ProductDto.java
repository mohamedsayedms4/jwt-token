package org.example.mobilyecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.mobilyecommerce.model.Icons;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    private Long id;
    private String title;
    private String description;
    private Long purchasPrice;
    private Long sellingPrice;
    private Integer discountPercentage;
    private Integer quantity;
    private String color;
    private Long categoryId;
    private Long viewsCounter;
    private Long searchCounter;
    private List<String> images;
    private Boolean isVerified;
    private Icons icons;

}

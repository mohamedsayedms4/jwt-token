package org.example.mobilyecommerce.mapper;

import org.example.mobilyecommerce.dto.ProductDto;
import org.example.mobilyecommerce.model.Product;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    ProductDto toDto(Product product);

    @Mapping(target = "category.id", source = "categoryId")
    Product toEntity(ProductDto dto);

}

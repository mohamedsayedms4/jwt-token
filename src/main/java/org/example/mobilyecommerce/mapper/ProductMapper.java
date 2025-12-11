package org.example.mobilyecommerce.mapper;

import org.example.mobilyecommerce.dto.ProductDto;
import org.example.mobilyecommerce.model.Product;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "iconsId" ,source = "icons.id")
    ProductDto toDto(Product product);

    @Mapping(target = "category.id", source = "categoryId")
    @Mapping(target = "icons.id", source = "iconsId")
    Product toEntity(ProductDto dto);

}

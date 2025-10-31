package org.example.mobilyecommerce.mapper;

import org.example.mobilyecommerce.dto.CategoryDto;
import org.example.mobilyecommerce.model.Category;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for converting between Category and CategoryDto.
 * Automatically handles nested children and parent ID.
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    /**
     * Converts a Category entity to a CategoryDto.
     * Uses recursive mapping for child categories.
     */
    @Mapping(source = "parentCategory.id", target = "parentId")
    CategoryDto toDto(Category category);

    /**
     * Converts a list of Category entities to a list of CategoryDto.
     */
    List<CategoryDto> toDtoList(List<Category> categories);

    /**
     * Converts a CategoryDto to a Category entity.
     * Note: parentCategory should be set manually in the service layer.
     */
    @Mapping(target = "parentCategory", ignore = true)
    @Mapping(target = "children", ignore = true) // Prevent infinite recursion
    Category toEntity(CategoryDto dto);

    /**
     * Converts a list of CategoryDto to a list of Category entities.
     */
    List<Category> toEntityList(List<CategoryDto> dtos);
}

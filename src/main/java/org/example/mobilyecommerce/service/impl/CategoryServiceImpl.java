package org.example.mobilyecommerce.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mobilyecommerce.dto.CategoryDto;
import org.example.mobilyecommerce.mapper.CategoryMapper;
import org.example.mobilyecommerce.model.Category;
import org.example.mobilyecommerce.repository.CategoryRepository;
import org.example.mobilyecommerce.service.CategoryService;
import org.example.mobilyecommerce.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final FileService fileService;

    @Override
    public Optional<CategoryDto> createCategory(CategoryDto categoryDto, MultipartFile icon) {
        try {
            Category category = categoryMapper.toEntity(categoryDto);

            // handle parent category
            if (categoryDto.getParentId() != null) {
                categoryRepository.findById(categoryDto.getParentId())
                        .ifPresent(category::setParentCategory);
            }

            // upload image
            if (icon != null && !icon.isEmpty()) {
                String imageUrl = fileService.uploadFile(icon);
                category.setImageUrl(imageUrl);
            }

            Category saved = categoryRepository.save(category);
            return Optional.of(categoryMapper.toDto(saved));
        } catch (IOException e) {
            log.error("Error uploading file for category: {}", categoryDto.getNameEn(), e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error creating category: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<CategoryDto> updateCategory(Long id, CategoryDto categoryDto, MultipartFile icon) {
        return categoryRepository.findById(id).map(existing -> {
            try {
                // update fields
                existing.setNameEn(categoryDto.getNameEn());
                existing.setNameAr(categoryDto.getNameAr());
                existing.setCategoryId(categoryDto.getCategoryId());
                existing.setLevel(categoryDto.getLevel());

                // update parent
                if (categoryDto.getParentId() != null) {
                    categoryRepository.findById(categoryDto.getParentId())
                            .ifPresent(existing::setParentCategory);
                } else {
                    existing.setParentCategory(null);
                }

                // update image if new one provided
                if (icon != null && !icon.isEmpty()) {
                    if (existing.getImageUrl() != null) {
                        String oldFileName = existing.getImageUrl().replace("/api/files/", "");
                        fileService.deleteFile(oldFileName);
                    }
                    String newUrl = fileService.uploadFile(icon);
                    existing.setImageUrl(newUrl);
                }

                Category updated = categoryRepository.save(existing);
                return categoryMapper.toDto(updated);

            } catch (IOException e) {
                log.error("Error updating category file: {}", e.getMessage());
                return null;
            }
        });
    }

    @Override
    public Boolean deleteCategory(Long id) {
        return categoryRepository.findById(id).map(category -> {
            try {
                // delete image if exists
                if (category.getImageUrl() != null) {
                    String fileName = category.getImageUrl().replace("/api/files/", "");
                    fileService.deleteFile(fileName);
                }
                categoryRepository.delete(category);
                return true;
            } catch (Exception e) {
                log.error("Error deleting category: {}", e.getMessage());
                return false;
            }
        }).orElse(false);
    }

    @Override
    public Optional<CategoryDto> getCategory(Long id) {
        return categoryRepository.findById(id).map(categoryMapper::toDto);
    }

    @Override
    public List<CategoryDto> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categoryMapper.toDtoList(categories);
    }
}

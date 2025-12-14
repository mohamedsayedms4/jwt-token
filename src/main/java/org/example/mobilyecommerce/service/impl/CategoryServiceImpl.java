package org.example.mobilyecommerce.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mobilyecommerce.dto.CategoryDto;
import org.example.mobilyecommerce.mapper.CategoryMapper;
import org.example.mobilyecommerce.model.Category;
import org.example.mobilyecommerce.repository.CategoryRepository;
import org.example.mobilyecommerce.service.CategoryService;
import org.example.mobilyecommerce.service.FileService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final FileService fileService;

    @Override
    @Caching(evict = {
            @CacheEvict(value = "categories", allEntries = true),
            @CacheEvict(value = "rootCategories", allEntries = true),
            @CacheEvict(value = "childCategoryIds", allEntries = true)
    })
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
    @Caching(evict = {
            @CacheEvict(value = "category", key = "#id"),
            @CacheEvict(value = "categories", allEntries = true),
            @CacheEvict(value = "rootCategories", allEntries = true),
            @CacheEvict(value = "childCategoryIds", allEntries = true)
    })
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
                        String oldFileName = extractFileName(existing.getImageUrl());
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
    @Caching(evict = {
            @CacheEvict(value = "category", key = "#id"),
            @CacheEvict(value = "categories", allEntries = true),
            @CacheEvict(value = "rootCategories", allEntries = true),
            @CacheEvict(value = "childCategoryIds", allEntries = true)
    })
    public Boolean deleteCategory(Long id) {
        return categoryRepository.findById(id).map(category -> {
            try {
                // delete image if exists
                if (category.getImageUrl() != null) {
                    String fileName = extractFileName(category.getImageUrl());
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
    @Cacheable(value = "category", key = "#id")
    public Optional<CategoryDto> getCategory(Long id) {
        return categoryRepository.findById(id).map(categoryMapper::toDto);
    }

    @Override
    @Cacheable(value = "categories")
    public List<CategoryDto> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categoryMapper.toDtoList(categories);
    }

    @Override
    @Cacheable(value = "rootCategories")
    public List<CategoryDto> getRootCategories() {
        return categoryRepository.findByParentCategoryIsNull().stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "childCategoryIds", key = "#parentId")
    public List<Long> getAllChildCategoryIds(Long parentId) {
        List<Long> ids = new ArrayList<>();
        List<Category> children = categoryRepository.findByParentCategoryId(parentId);
        for (Category child : children) {
            ids.add(child.getId());
            ids.addAll(getAllChildCategoryIds(child.getId())); // recursion
        }
        return ids;
    }

    // Helper method to extract filename from URL
    private String extractFileName(String imageUrl) {
        if (imageUrl.startsWith("/api/files/")) {
            return imageUrl.replace("/api/files/", "");
        } else if (imageUrl.contains("/api/files/")) {
            return imageUrl.substring(imageUrl.lastIndexOf("/api/files/") + 11);
        } else {
            return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        }
    }
}
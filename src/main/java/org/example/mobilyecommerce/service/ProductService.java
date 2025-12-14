package org.example.mobilyecommerce.service;

import org.example.mobilyecommerce.dto.ProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    Optional<ProductDto> insert(
            ProductDto  productDto , List<MultipartFile> images , List<MultipartFile> imagesDetails);

    Optional<ProductDto> findById(Long id);


    Page<ProductDto> getProducts(Pageable pageable);


    Page<ProductDto> findByCategoryId(Long categoryId, Pageable pageable);

    Page<ProductDto> findByViewsCounter(Pageable pageable);

    Page<ProductDto> findBySearchCounter(Pageable pageable);

    Page<ProductDto> findByIsVerified(Boolean isVerified, Pageable pageable);

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    boolean updateProductStatus(Long id , boolean status);


    Optional<ProductDto> updateProduct(Long id, ProductDto createProductDto);

    void deleteProduct(Long id);

    Page<ProductDto> getProductsByCategory(Long categoryId, int page, int size);
    Page<ProductDto> getProductsByParentCategory(Long parentCategoryId, int page, int size);


}

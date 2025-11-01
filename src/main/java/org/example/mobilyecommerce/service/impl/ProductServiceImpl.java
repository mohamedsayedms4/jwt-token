package org.example.mobilyecommerce.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.mobilyecommerce.dto.ProductDto;
import org.example.mobilyecommerce.mapper.ProductMapper;
import org.example.mobilyecommerce.model.Category;
import org.example.mobilyecommerce.model.Product;
import org.example.mobilyecommerce.repository.CategoryRepository;
import org.example.mobilyecommerce.repository.ProductRepository;
import org.example.mobilyecommerce.service.FileService;
import org.example.mobilyecommerce.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final FileService fileService;

    @Override
    public Optional<ProductDto> insert(ProductDto productDto, List<MultipartFile> images) {
        Category category = categoryRepository.findById(productDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Product product = productMapper.toEntity(productDto);
        product.setId(null); // ✅ مهم جداً: تأكد أنه منتج جديد
        product.setCategory(category);

        // ✅ حفظ الصور إن وُجدت
        if (images != null && !images.isEmpty()) {
            List<String> uploadedUrls = uploadImages(images);
            product.setImages(uploadedUrls);
        }

        Product saved = productRepository.save(product);
        return Optional.of(productMapper.toDto(saved));
    }

    @Override
    public Optional<ProductDto> findById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toDto);
    }

    @Override
    public Page<ProductDto> getProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(productMapper::toDto);
    }

    @Override
    public Page<ProductDto> findByCategoryId(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable)
                .map(productMapper::toDto);
    }

    @Override
    public Page<ProductDto> findByViewsCounter(Pageable pageable) {
        return productRepository.findByViewsCounterGreaterThanOrderByViewsCounterDesc(0L, pageable)
                .map(productMapper::toDto);
    }

    @Override
    public Page<ProductDto> findBySearchCounter(Pageable pageable) {
        return productRepository.findBySearchCounterGreaterThanOrderBySearchCounterDesc(0L, pageable)
                .map(productMapper::toDto);
    }

    @Override
    public Page<ProductDto> findByIsVerified(Boolean isVerified, Pageable pageable) {
        return productRepository.findByIsVerified(isVerified, pageable)
                .map(productMapper::toDto);
    }

    @Override
    public boolean updateProductStatus(Long id, boolean status) {
        return productRepository.findById(id).map(product -> {
            product.setIsVerified(status);
            productRepository.save(product);
            return true;
        }).orElse(false);
    }

    @Override
    public Optional<ProductDto> updateProduct(Long id, ProductDto dto) {
        return productRepository.findById(id).map(product -> {
            product.setTitle(dto.getTitle());
            product.setDescription(dto.getDescription());
            product.setSellingPrice(dto.getSellingPrice());
            product.setPurchasPrice(dto.getPurchasPrice());
            product.setDiscountPercentage(dto.getDiscountPercentage());
            product.setQuantity(dto.getQuantity());
            product.setColor(dto.getColor());

            // تحديث الصور لو أرسلت جديدة
            if (dto.getImages() != null && !dto.getImages().isEmpty()) {
                product.setImages(dto.getImages());
            }

            Product updated = productRepository.save(product);
            return productMapper.toDto(updated);
        });
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.findById(id).ifPresent(product -> {
            // حذف الصور من السيرفر قبل حذف المنتج
            if (product.getImages() != null) {
                product.getImages().forEach(fileService::deleteFile);
            }
            productRepository.deleteById(id);
        });
    }

    // 🔹 ميثود مساعدة لرفع الصور
    private List<String> uploadImages(List<MultipartFile> images) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : images) {
            try {
                String url = fileService.uploadFile(file);
                urls.add(url);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload image: " + file.getOriginalFilename(), e);
            }
        }
        return urls;
    }
}

package org.example.mobilyecommerce.repository;

import org.example.mobilyecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
    Page<Product> findByViewsCounterGreaterThanOrderByViewsCounterDesc(Long minViews, Pageable pageable);
    Page<Product> findBySearchCounterGreaterThanOrderBySearchCounterDesc(Long minSearch, Pageable pageable);
    Page<Product> findByIsVerified(Boolean isVerified, Pageable pageable);

}

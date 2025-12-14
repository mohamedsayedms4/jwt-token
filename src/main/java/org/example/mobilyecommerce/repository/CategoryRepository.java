package org.example.mobilyecommerce.repository;

import org.example.mobilyecommerce.model.Category;
import org.example.mobilyecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByParentCategoryIsNull();
    List<Category> findByParentCategoryId(Long parentId);

}

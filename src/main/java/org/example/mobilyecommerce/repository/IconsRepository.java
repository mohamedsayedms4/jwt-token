package org.example.mobilyecommerce.repository;

import org.example.mobilyecommerce.model.Icons;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IconsRepository extends JpaRepository<Icons, Long> {
}

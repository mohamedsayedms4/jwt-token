package org.example.mobilyecommerce.repository;

import org.example.mobilyecommerce.model.HomePage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HomePageRepository extends JpaRepository<HomePage, Long> {
}

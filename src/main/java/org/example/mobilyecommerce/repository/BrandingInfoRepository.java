package org.example.mobilyecommerce.repository;

import jakarta.persistence.LockModeType;
import org.example.mobilyecommerce.model.BrandingInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandingInfoRepository extends JpaRepository<BrandingInfo, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BrandingInfo b where b.id = :id")
    BrandingInfo findByIdForUpdate(@Param("id") Long id);

}

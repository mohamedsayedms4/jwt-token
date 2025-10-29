package org.example.mobilyecommerce.repository;

import org.example.mobilyecommerce.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    /**
     * البحث عن جميع tokens للمستخدم (للتشخيص)
     */
    List<RefreshToken> findByUserId(Long userId);

    /**
     * ✅ الطريقة 1: استخدام Spring Data JPA naming convention
     * هذه الطريقة أفضل وأبسط
     */
    void deleteByUserId(Long userId);

    /**
     * ✅ الطريقة 2: استخدام @Query (بديلة)
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);

    /**
     * Mark refresh tokens as expired where expiryDate has passed
     */
    @Modifying
    @Query("""
        UPDATE RefreshToken rt
        SET rt.expired = true
        WHERE rt.expiryDate < :now
        AND rt.expired = false
        """)
    int markExpiredRefreshTokens(@Param("now") Instant now);

    /**
     * Delete refresh tokens that are marked as expired
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expired = true")
    int deleteExpiredRefreshTokens();
}
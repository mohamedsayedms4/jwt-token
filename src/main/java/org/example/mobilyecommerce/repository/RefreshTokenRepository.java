package org.example.mobilyecommerce.repository;

import jakarta.transaction.Transactional;
import org.example.mobilyecommerce.model.RefreshToken;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * ✅ البحث عن refresh token بقيمته
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * ✅ حذف refresh token لمستخدم محدد
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    /**
     * ✅ تعليم الـ refresh tokens اللي انتهت صلاحيتها
     */
    @Modifying
    @Transactional
    @Query("""
        UPDATE RefreshToken rt
        SET rt.expired = true
        WHERE rt.expiryDate <= :now
        AND rt.expired = false
    """)
    int markExpiredRefreshTokens(@Param("now") Instant now);

    /**
     * ✅ حذف جميع الـ refresh tokens المنتهية أو المعلّمة
     */
    @Modifying
    @Transactional
    @Query("""
        DELETE FROM RefreshToken rt
        WHERE rt.expired = true OR rt.expiryDate <= :now
    """)
    int deleteExpiredRefreshTokens(@Param("now") Instant now);

    /**
     * ✅ حذف جميع الـ refresh tokens الأقدم من تاريخ معين (اختياري)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    int deleteAllByExpiryDateBefore(@Param("now") Instant now);
}

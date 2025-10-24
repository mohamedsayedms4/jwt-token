package org.example.mobilyecommerce.repository;

import jakarta.transaction.Transactional;
import org.example.mobilyecommerce.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    /**
     * ✅ البحث عن توكن بقيمته
     */
    Optional<Token> findByToken(String token);

    /**
     * ✅ جلب جميع التوكنات الصالحة للمستخدم
     */
    @Query("""
        SELECT t FROM Token t
        WHERE t.user.id = :userId AND (t.expired = false AND t.revoked = false)
    """)
    List<Token> findAllValidTokenByUser(@Param("userId") Long userId);

    /**
     * ✅ حذف جميع التوكنات للمستخدم (عند تسجيل الخروج)
     */
    @Modifying
    @Query("DELETE FROM Token t WHERE t.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    /**
     * ✅ تحديث التوكنات اللي انتهت صلاحيتها (يعلّمها expired = true)
     * يرجع عدد التوكنات اللي اتحدثت
     */
    @Modifying
    @Transactional
    @Query("""
        UPDATE Token t 
        SET t.expired = true 
        WHERE t.expiryDate <= :now 
        AND t.expired = false
    """)
    int markExpiredTokens(@Param("now") Instant now);

    /**
     * ✅ حذف التوكنات المنتهية أو الملغاة
     */
    @Modifying
    @Transactional
    @Query("""
        DELETE FROM Token t
        WHERE t.expired = true OR t.revoked = true
    """)
    int deleteExpiredAndRevokedTokens();
}
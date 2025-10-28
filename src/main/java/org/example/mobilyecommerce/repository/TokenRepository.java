package org.example.mobilyecommerce.repository;

import org.example.mobilyecommerce.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByToken(String token);

    @Query("""
        SELECT t FROM Token t
        WHERE t.user.id = :userId 
        AND t.expired = false 
        AND t.revoked = false
    """)
    List<Token> findAllValidTokenByUser(@Param("userId") Long userId);

    /**
     * Mark tokens as expired where expiryDate has passed
     * ⚠️ استخدم < بدل <= عشان نتجنب مشاكل الـ timing
     */
    @Modifying
    @Query("""
        UPDATE Token t
        SET t.expired = true
        WHERE t.expiryDate < :now
        AND t.expired = false
        """)
    int markExpiredTokens(@Param("now") Instant now);

    /**
     * Delete tokens that are expired OR revoked
     */
    @Modifying
    @Query("DELETE FROM Token t WHERE t.expired = true OR t.revoked = true")
    int deleteExpiredAndRevokedTokens();


}
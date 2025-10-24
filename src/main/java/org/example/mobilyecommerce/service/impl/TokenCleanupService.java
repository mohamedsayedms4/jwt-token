package org.example.mobilyecommerce.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mobilyecommerce.config.iwt.JwtToken;
import org.example.mobilyecommerce.repository.TokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Service responsible for automatic cleanup of expired or revoked access tokens.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TokenCleanupService {

    private final TokenRepository tokenRepository;
    private final JwtToken jwtToken;

    /**
     * Stage 1: Mark expired tokens
     * Runs every minute, sets expired = true for tokens past their expiry date
     */
    @Scheduled(cron = "0 * * * * *") // every minute
    @Transactional
    public void markExpiredTokens() {
        try {
            log.debug("🔍 Checking for expired tokens at: {}", Instant.now());

            int updatedCount = tokenRepository.markExpiredTokens(Instant.now());

            if (updatedCount > 0) {
                log.info("⚠️ Marked {} tokens as expired", updatedCount);
            } else {
                log.debug("ℹ️ No tokens needed marking as expired");
            }
        } catch (Exception e) {
            log.error("❌ Error while marking expired tokens: {}", e.getMessage(), e);
        }
    }

    /**
     * Stage 2: Delete expired or revoked tokens
     * Runs every 2 minutes (adjust cron as needed)
     */
    @Scheduled(cron = "0 */2 * * * *") // every 2 minutes
    @Transactional
    public void deleteExpiredTokens() {
        try {
            log.info("🧹 Starting deletion of expired/revoked tokens at: {}", Instant.now());

            int deletedCount = tokenRepository.deleteExpiredAndRevokedTokens();

            if (deletedCount > 0) {
                log.info("✅ Deleted {} expired/revoked tokens from database", deletedCount);
            } else {
                log.debug("ℹ️ No expired/revoked tokens to delete");
            }
        } catch (Exception e) {
            log.error("❌ Error while deleting expired/revoked tokens: {}", e.getMessage(), e);
        }
    }

    /**
     * Immediate/manual cleanup for expired/revoked tokens
     */
    @Transactional
    public void cleanupNow() {
        log.info("🧹 Performing immediate cleanup of tokens...");

        int marked = tokenRepository.markExpiredTokens(Instant.now());
        log.info("⚠️ Marked {} tokens as expired", marked);

        int deleted = tokenRepository.deleteExpiredAndRevokedTokens();
        log.info("✅ Immediate cleanup: deleted {} tokens", deleted);
    }
}

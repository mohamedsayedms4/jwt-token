package org.example.mobilyecommerce.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mobilyecommerce.repository.RefreshTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Service responsible for automatic cleanup of expired refresh tokens.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenCleanupService {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Stage 1: Mark expired refresh tokens (every 30 minutes)
     * Note: Cron here is every 30 minutes (adjust if you want 5 min interval)
     */
    @Scheduled(cron = "0 */30 * * * *") // every 30 minutes
    @Transactional
    public void markExpiredRefreshTokens() {
        try {
            log.debug("🔍 Checking for expired refresh tokens at: {}", Instant.now());

            int updatedCount = refreshTokenRepository.markExpiredRefreshTokens(Instant.now());

            if (updatedCount > 0) {
                log.info("⚠️ Marked {} refresh tokens as expired", updatedCount);
            } else {
                log.debug("ℹ️ No refresh tokens needed marking as expired");
            }
        } catch (Exception e) {
            log.error("❌ Error while marking expired refresh tokens: {}", e.getMessage(), e);
        }
    }

    /**
     * Stage 2: Delete expired refresh tokens (every 30 minutes)
     */
    @Scheduled(cron = "0 */30 * * * *") // every 30 minutes
    @Transactional
    public void deleteExpiredRefreshTokens() {
        try {
            log.info("🧹 Starting deletion of expired refresh tokens at: {}", Instant.now());

            int deletedCount = refreshTokenRepository.deleteExpiredRefreshTokens(Instant.now());

            if (deletedCount > 0) {
                log.info("✅ Deleted {} expired refresh tokens from database", deletedCount);
            } else {
                log.debug("ℹ️ No expired refresh tokens to delete");
            }
        } catch (Exception e) {
            log.error("❌ Error while deleting expired refresh tokens: {}", e.getMessage(), e);
        }
    }

    /**
     * Immediate/manual cleanup of expired refresh tokens
     */
    @Transactional
    public void cleanupNow() {
        log.info("🧹 Performing immediate cleanup of refresh tokens...");

        Instant now = Instant.now();

        int marked = refreshTokenRepository.markExpiredRefreshTokens(now);
        log.info("⚠️ Marked {} refresh tokens as expired", marked);

        int deleted = refreshTokenRepository.deleteExpiredRefreshTokens(now);
        log.info("✅ Immediate cleanup: deleted {} refresh tokens", deleted);
    }
}

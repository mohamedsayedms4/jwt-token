package org.example.mobilyecommerce.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mobilyecommerce.service.RefreshTokenServiceInterface;
import org.example.mobilyecommerce.service.TokenCleanupServiceInterface;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Centralized scheduler for token cleanup operations
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final TokenCleanupServiceInterface accessTokenCleanupService;
    private final RefreshTokenServiceInterface refreshTokenService;

    /**
     * Mark expired access tokens (every 1 minute)
     */
    @Scheduled(cron = "0 */30 * * * *")
    public void markExpiredAccessTokens() {
        try {
            accessTokenCleanupService.markExpiredTokens();
        } catch (Exception e) {
            log.error("❌ Error marking expired access tokens: {}", e.getMessage(), e);
        }
    }

    /**
     * Delete expired/revoked access tokens (every 2 minutes)
     */
    @Scheduled(cron = "0 */30 * * * *")
    public void deleteExpiredAccessTokens() {
        try {
            accessTokenCleanupService.deleteExpiredTokens();
        } catch (Exception e) {
            log.error("❌ Error deleting expired access tokens: {}", e.getMessage(), e);
        }
    }

    /**
     * Mark expired refresh tokens (every 30 minutes)
     */
    @Scheduled(cron = "0 */30 * * * *")
    public void markExpiredRefreshTokens() {
        try {
            refreshTokenService.markExpiredTokens();
        } catch (Exception e) {
            log.error("❌ Error marking expired refresh tokens: {}", e.getMessage(), e);
        }
    }

    /**
     * Delete expired refresh tokens (every 30 minutes)
     */
    @Scheduled(cron = "0 */30 * * * *")
    public void deleteExpiredRefreshTokens() {
        try {
            refreshTokenService.deleteExpiredTokens();
        } catch (Exception e) {
            log.error("❌ Error deleting expired refresh tokens: {}", e.getMessage(), e);
        }
    }
}
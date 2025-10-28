//package org.example.mobilyecommerce.service.impl;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.example.mobilyecommerce.repository.RefreshTokenRepository;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.Instant;
//
///**
// * Service responsible for automatic cleanup of expired refresh tokens.
// */
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class RefreshTokenCleanupService {
//
//    private final RefreshTokenRepository refreshTokenRepository;
//
//    /**
//     * Stage 1: Mark expired refresh tokens (every 2 minutes for testing)
//     */
//    @Scheduled(cron = "0 */5 * * * *")
//    @Transactional
//    public void markExpiredRefreshTokens() {
//        try {
//            Instant now = Instant.now();
//            log.debug("🔍 Checking for expired refresh tokens at: {}", now);
//
//            int updatedCount = refreshTokenRepository.markExpiredRefreshTokens(now);
//
//            if (updatedCount > 0) {
//                log.info("⚠️ Marked {} refresh tokens as expired", updatedCount);
//            } else {
//                log.debug("ℹ️ No refresh tokens needed marking as expired");
//            }
//        } catch (Exception e) {
//            log.error("❌ Error while marking expired refresh tokens: {}", e.getMessage(), e);
//        }
//    }
//
//    /**
//     * Stage 2: Delete expired refresh tokens (every 2 minutes for testing)
//     */
//    @Scheduled(cron = "0 */5 * * * *")
//    @Transactional
//    public void deleteExpiredRefreshTokens() {
//        try {
//            log.info("🧹 Starting deletion of expired refresh tokens at: {}", Instant.now());
//
//            // ⭐ احذف الـ parameter - الـ method ما بتاخد parameters
//            int deletedCount = refreshTokenRepository.deleteExpiredRefreshTokens();
//
//            if (deletedCount > 0) {
//                log.info("✅ Deleted {} expired refresh tokens from database", deletedCount);
//            } else {
//                log.debug("ℹ️ No expired refresh tokens to delete");
//            }
//        } catch (Exception e) {
//            log.error("❌ Error while deleting expired refresh tokens: {}", e.getMessage(), e);
//        }
//    }
//
//
//}
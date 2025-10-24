package org.example.mobilyecommerce.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mobilyecommerce.repository.RefreshTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * ✅ سيرفس مسؤول عن تنظيف الـ Refresh Tokens المنتهية تلقائياً
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenCleanupService {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * ✅ المرحلة الأولى: تعليم الـ refresh tokens المنتهية (كل 5 دقائق)
     */
    @Scheduled(cron = "0 */30 * * * *") // ⏱️ كل 5 دقائق
    @Transactional
    public void markExpiredRefreshTokens() {
        try {
            log.debug("🔍 فحص الـ refresh tokens المنتهية عند: {}", Instant.now());

            int updatedCount = refreshTokenRepository.markExpiredRefreshTokens(Instant.now());

            if (updatedCount > 0) {
                log.info("⚠️ تم تعليم {} refresh token كـ منتهي الصلاحية", updatedCount);
            } else {
                log.debug("ℹ️ لا توجد refresh tokens تحتاج تعليم كـ منتهية");
            }
        } catch (Exception e) {
            log.error("❌ خطأ أثناء تحديث الـ refresh tokens المنتهية: {}", e.getMessage(), e);
        }
    }

    /**
     * ✅ المرحلة الثانية: حذف الـ refresh tokens المنتهية (كل 30 دقيقة)
     */
    @Scheduled(cron = "0 */30 * * * *") // ⏱️ كل 30 دقيقة
    @Transactional
    public void deleteExpiredRefreshTokens() {
        try {
            log.info("🧹 بدأ حذف الـ refresh tokens المنتهية عند: {}", Instant.now());

            int deletedCount = refreshTokenRepository.deleteExpiredRefreshTokens(Instant.now());

            if (deletedCount > 0) {
                log.info("✅ تم حذف {} refresh token منتهي من قاعدة البيانات", deletedCount);
            } else {
                log.debug("ℹ️ لا توجد refresh tokens منتهية للحذف");
            }
        } catch (Exception e) {
            log.error("❌ خطأ أثناء حذف الـ refresh tokens المنتهية: {}", e.getMessage(), e);
        }
    }

    /**
     * ✅ تنظيف فوري (يدوي)
     */
    @Transactional
    public void cleanupNow() {
        log.info("🧹 بدء التنظيف الفوري للـ refresh tokens...");

        Instant now = Instant.now();

        int marked = refreshTokenRepository.markExpiredRefreshTokens(now);
        log.info("⚠️ تم تعليم {} refresh token كـ منتهي", marked);

        int deleted = refreshTokenRepository.deleteExpiredRefreshTokens(now);
        log.info("✅ تنظيف فوري: تم حذف {} refresh token", deleted);
    }
}

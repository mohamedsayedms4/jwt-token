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
 * ✅ سيرفس مخصص لتنظيف التوكنات المنتهية تلقائياً
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TokenCleanupService {

    private final TokenRepository tokenRepository;
    private final JwtToken jwtToken;

    /**
     * ✅ المرحلة الأولى: تحديث التوكنات المنتهية (كل دقيقة)
     * يعلّم التوكنات اللي انتهت صلاحيتها بـ expired = true
     */
    @Scheduled(cron = "0 * * * * *") // ⏱️ كل دقيقة
    @Transactional
    public void markExpiredTokens() {
        try {
            log.debug("🔍 فحص التوكنات المنتهية عند: {}", Instant.now());

            // تحديث التوكنات اللي تاريخها خلص
            int updatedCount = tokenRepository.markExpiredTokens(Instant.now());

            if (updatedCount > 0) {
                log.info("⚠️ تم تعليم {} توكن كـ منتهي الصلاحية", updatedCount);
            }
        } catch (Exception e) {
            log.error("❌ خطأ أثناء تحديث التوكنات المنتهية: {}", e.getMessage(), e);
        }
    }

    /**
     * ✅ المرحلة الثانية: حذف التوكنات المنتهية (كل 5 دقائق)
     * يمسح التوكنات اللي اتعلّمت expired = true من فترة
     */
    @Scheduled(cron = "0 */2 * * * *") // ⏱️ كل 5 دقائق
    @Transactional
    public void deleteExpiredTokens() {
        try {
            log.info("🧹 بدأ حذف التوكنات المنتهية عند: {}", Instant.now());

            // حذف التوكنات المنتهية أو الملغاة
            int deletedCount = tokenRepository.deleteExpiredAndRevokedTokens();

            if (deletedCount > 0) {
                log.info("✅ تم حذف {} توكن منتهي من قاعدة البيانات", deletedCount);
            } else {
                log.debug("ℹ️ لا توجد توكنات منتهية للحذف");
            }
        } catch (Exception e) {
            log.error("❌ خطأ أثناء حذف التوكنات المنتهية: {}", e.getMessage(), e);
        }
    }

    /**
     * ✅ حذف فوري لجميع التوكنات المنتهية (يمكن استدعاؤه يدوياً)
     */
    @Transactional
    public void cleanupNow() {
        log.info("🧹 بدء التنظيف الفوري للتوكنات...");

        // أولاً: تعليم المنتهية
        int marked = tokenRepository.markExpiredTokens(Instant.now());
        log.info("⚠️ تم تعليم {} توكن", marked);

        // ثانياً: حذفها
        int deleted = tokenRepository.deleteExpiredAndRevokedTokens();
        log.info("✅ تنظيف فوري: تم حذف {} توكن", deleted);
    }
}
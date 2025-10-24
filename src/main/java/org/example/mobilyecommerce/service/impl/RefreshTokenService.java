package org.example.mobilyecommerce.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.mobilyecommerce.model.RefreshToken;
import org.example.mobilyecommerce.model.User;
import org.example.mobilyecommerce.repository.RefreshTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    // ✅ إنشاء refresh token بشكل عشوائي بنمط xxxx-xxxx-xxxx-xxxx
    public RefreshToken createRefreshToken(User user) {
        String token = generateTokenFormat();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiryDate(Instant.now().plusSeconds(60 * 60 * 24 * 7)) // أسبوع مثلاً

                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    // ✅ حذف أو تعطيل التوكن عند تسجيل الخروج
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUserId(user.getId());
    }

    // ✅ حذف التوكنات المنتهية تلقائياً (تعمل كل ساعة)
    @Scheduled(cron = "0 */10 * * * *") // ⏱️ كل 5 دقائق
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteAllByExpiryDateBefore(Instant.now());
    }

    // ✅ التحقق من صلاحية التوكن
    public boolean isValid(String token) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(token);
        return refreshToken.isPresent() && refreshToken.get().getExpiryDate().isAfter(Instant.now());
    }

    // ✅ شكل التوكن xxxx-xxxx-xxxx-xxxx
    private String generateTokenFormat() {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            formatted.append(uuid.charAt(i));
            if ((i + 1) % 4 == 0 && i < 15) formatted.append("-");
        }
        return formatted.toString();
    }
}

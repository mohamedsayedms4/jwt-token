package org.example.mobilyecommerce.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * ✅ Create a random refresh token in the format xxxx-xxxx-xxxx-xxxx
     * Expiry example: 7 days
     */
    public RefreshToken createRefreshToken(User user) {
        String token = generateTokenFormat();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiryDate(Instant.now().plusSeconds(60 * 60 * 24 * 7)) // 7 days
                .build();
        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        log.debug("💾 Created new refresh token for user: {}", user.getUsername());
        return savedToken;
    }

    /**
     * ✅ Delete all refresh tokens for a user (e.g., on logout)
     */
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUserId(user.getId());
        log.debug("🗑️ Deleted all refresh tokens for user: {}", user.getUsername());
    }

    /**
     * ✅ Scheduled cleanup of expired refresh tokens
     * Runs every 10 minutes (adjust cron as needed)
     */
    @Scheduled(cron = "0 */10 * * * *") // every 10 minutes
    public void deleteExpiredTokens() {
        int deleted = refreshTokenRepository.deleteAllByExpiryDateBefore(Instant.now());
        log.debug("🧹 Deleted {} expired refresh tokens", deleted);
    }

    /**
     * ✅ Check if a refresh token is valid (exists and not expired)
     */
    public boolean isValid(String token) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(token);
        boolean valid = refreshToken.isPresent() && refreshToken.get().getExpiryDate().isAfter(Instant.now());
        log.debug("🔍 Refresh token {} is valid: {}", token, valid);
        return valid;
    }

    /**
     * ✅ Generate token in format xxxx-xxxx-xxxx-xxxx
     */
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

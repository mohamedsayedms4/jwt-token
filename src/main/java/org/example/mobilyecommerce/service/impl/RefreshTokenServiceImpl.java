package org.example.mobilyecommerce.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mobilyecommerce.config.iwt.TokenHandler;
import org.example.mobilyecommerce.exception.TokenNotFoundException;
import org.example.mobilyecommerce.model.RefreshToken;
import org.example.mobilyecommerce.model.User;
import org.example.mobilyecommerce.repository.RefreshTokenRepository;
import org.example.mobilyecommerce.service.RefreshTokenServiceInterface;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenServiceInterface {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHandler tokenHandler;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        log.debug("🔑 Creating refresh token for user: {}", user.getUsername());

        // حذف أي refresh tokens قديمة للمستخدم
//        refreshTokenRepository.deleteByUserId(user.getId());

        String tokenValue = generateReadableToken();
        Instant now = Instant.now();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(tokenValue)
                .createdAt(now)
                .expiryDate(now.plus(tokenHandler.getRefreshTime()))
                .expired(false)
                .build();

        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        log.debug("✅ Refresh token created for user: {} (expires: {})", user.getUsername(), saved.getExpiryDate());

        return saved;
    }

    @Override
    public RefreshToken getByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenNotFoundException("Refresh token not found"));
    }

    @Override
    @Transactional
    public void deleteByUser(User user) {
        log.debug("🗑️ Deleting refresh tokens for user: {}", user.getUsername());
        refreshTokenRepository.deleteByUserId(user.getId());
    }

    @Override
    public boolean isValid(String token) {
        return refreshTokenRepository.findByToken(token)
                .map(rt -> {
                    boolean notExpiredFlag = !rt.isExpired();
                    boolean notExpiredTime = rt.getExpiryDate().isAfter(Instant.now());
                    log.debug("🔍 Token validation - Flag: {}, Time: {}", notExpiredFlag, notExpiredTime);
                    return notExpiredFlag && notExpiredTime;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public int markExpiredTokens() {
        Instant now = Instant.now();
        log.info("🔍 Marking expired refresh tokens (current time: {})", now);

        int count = refreshTokenRepository.markExpiredRefreshTokens(now);

        if (count > 0) {
            log.info("⚠️ Marked {} refresh tokens as expired", count);
        } else {
            log.debug("ℹ️ No refresh tokens to mark as expired");
        }

        return count;
    }

    @Override
    @Transactional
    public int deleteExpiredTokens() {
        log.info("🧹 Deleting expired refresh tokens");

        int count = refreshTokenRepository.deleteExpiredRefreshTokens();

        if (count > 0) {
            log.info("✅ Deleted {} expired refresh tokens", count);
        } else {
            log.debug("ℹ️ No expired refresh tokens to delete");
        }

        return count;
    }

    private String generateReadableToken() {
        SecureRandom random = new SecureRandom();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder token = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            if (i > 0) token.append("-");
            for (int j = 0; j < 4; j++) {
                token.append(chars.charAt(random.nextInt(chars.length())));
            }
        }

        return token.toString();
    }
}
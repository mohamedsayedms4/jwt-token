package org.example.mobilyecommerce.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mobilyecommerce.repository.TokenRepository;
import org.example.mobilyecommerce.service.TokenCleanupServiceInterface;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenCleanupServiceImpl implements TokenCleanupServiceInterface {

    private final TokenRepository tokenRepository;

    @Override
    @Transactional
    public void markExpiredTokens() {
        Instant now = Instant.now();
        log.info("🔍 Marking expired access tokens (current time: {})", now);

        int updatedCount = tokenRepository.markExpiredTokens(now);

        if (updatedCount > 0) {
            log.info("⚠️ Marked {} access tokens as expired", updatedCount);
        } else {
            log.debug("ℹ️ No access tokens to mark as expired");
        }
    }

    @Override
    @Transactional
    public void deleteExpiredTokens() {
        log.info("🧹 Deleting expired/revoked access tokens");

        int deletedCount = tokenRepository.deleteExpiredAndRevokedTokens();

        if (deletedCount > 0) {
            log.info("✅ Deleted {} expired/revoked access tokens", deletedCount);
        } else {
            log.debug("ℹ️ No expired/revoked access tokens to delete");
        }
    }

    @Override
    @Transactional
    public void cleanupNow() {
        log.info("🧹 Performing immediate access token cleanup");

        Instant now = Instant.now();
        int marked = tokenRepository.markExpiredTokens(now);
        int deleted = tokenRepository.deleteExpiredAndRevokedTokens();

        log.info("✅ Immediate cleanup: marked {} and deleted {} access tokens", marked, deleted);
    }
}
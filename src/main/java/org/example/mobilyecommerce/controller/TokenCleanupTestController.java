package org.example.mobilyecommerce.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mobilyecommerce.service.RefreshTokenServiceInterface;
import org.example.mobilyecommerce.service.TokenCleanupServiceInterface;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Test endpoints for token cleanup operations
 * Use only in development/testing environments
 */
@RestController
@RequestMapping("/api/v1/admin/token-cleanup")
@RequiredArgsConstructor
@Slf4j
//@PreAuthorize("hasRole('ADMIN')")
public class TokenCleanupTestController {

    private final TokenCleanupServiceInterface accessTokenCleanupService;
    private final RefreshTokenServiceInterface refreshTokenService;

    /**
     * Test marking expired access tokens
     * POST /api/v1/admin/token-cleanup/access-tokens/mark-expired
     */
    @PostMapping("/access-tokens/mark-expired")
    public ResponseEntity<Map<String, Object>> markExpiredAccessTokens() {
        log.info("🧪 Manual test: Marking expired access tokens");

        try {
            accessTokenCleanupService.markExpiredTokens();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Expired access tokens marked successfully");
            response.put("operation", "mark_expired_access_tokens");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error marking expired access tokens: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Test deleting expired access tokens
     * POST /api/v1/admin/token-cleanup/access-tokens/delete-expired
     */
    @PostMapping("/access-tokens/delete-expired")
    public ResponseEntity<Map<String, Object>> deleteExpiredAccessTokens() {
        log.info("🧪 Manual test: Deleting expired access tokens");

        try {
            accessTokenCleanupService.deleteExpiredTokens();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Expired access tokens deleted successfully");
            response.put("operation", "delete_expired_access_tokens");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error deleting expired access tokens: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Test complete access token cleanup
     * POST /api/v1/admin/token-cleanup/access-tokens/cleanup-now
     */
    @PostMapping("/access-tokens/cleanup-now")
    public ResponseEntity<Map<String, Object>> cleanupAccessTokensNow() {
        log.info("🧪 Manual test: Complete access token cleanup");

        try {
            accessTokenCleanupService.cleanupNow();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Access token cleanup completed successfully");
            response.put("operation", "cleanup_access_tokens_now");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error during access token cleanup: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Test marking expired refresh tokens
     * POST /api/v1/admin/token-cleanup/refresh-tokens/mark-expired
     */
    @PostMapping("/refresh-tokens/mark-expired")
    public ResponseEntity<Map<String, Object>> markExpiredRefreshTokens() {
        log.info("🧪 Manual test: Marking expired refresh tokens");

        try {
            int marked = refreshTokenService.markExpiredTokens();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Expired refresh tokens marked successfully");
            response.put("markedCount", marked);
            response.put("operation", "mark_expired_refresh_tokens");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error marking expired refresh tokens: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Test deleting expired refresh tokens
     * POST /api/v1/admin/token-cleanup/refresh-tokens/delete-expired
     */
    @PostMapping("/refresh-tokens/delete-expired")
    public ResponseEntity<Map<String, Object>> deleteExpiredRefreshTokens() {
        log.info("🧪 Manual test: Deleting expired refresh tokens");

        try {
            int deleted = refreshTokenService.deleteExpiredTokens();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Expired refresh tokens deleted successfully");
            response.put("deletedCount", deleted);
            response.put("operation", "delete_expired_refresh_tokens");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error deleting expired refresh tokens: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Test all cleanup operations at once
     * POST /api/v1/admin/token-cleanup/all
     */
    @PostMapping("/all")
    public ResponseEntity<Map<String, Object>> cleanupAllTokens() {
        log.info("🧪 Manual test: Complete cleanup of all tokens");

        try {
            // Access tokens
            accessTokenCleanupService.cleanupNow();

            // Refresh tokens
            int marked = refreshTokenService.markExpiredTokens();
            int deleted = refreshTokenService.deleteExpiredTokens();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "All token cleanup completed successfully");
            response.put("refreshTokensMarked", marked);
            response.put("refreshTokensDeleted", deleted);
            response.put("operation", "cleanup_all_tokens");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error during complete cleanup: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get cleanup status/info
     * GET /api/v1/admin/token-cleanup/status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getCleanupStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("available_endpoints", new String[]{
                "POST /api/v1/admin/token-cleanup/access-tokens/mark-expired",
                "POST /api/v1/admin/token-cleanup/access-tokens/delete-expired",
                "POST /api/v1/admin/token-cleanup/access-tokens/cleanup-now",
                "POST /api/v1/admin/token-cleanup/refresh-tokens/mark-expired",
                "POST /api/v1/admin/token-cleanup/refresh-tokens/delete-expired",
                "POST /api/v1/admin/token-cleanup/all"
        });
        response.put("note", "All endpoints require ADMIN role");

        return ResponseEntity.ok(response);
    }
}
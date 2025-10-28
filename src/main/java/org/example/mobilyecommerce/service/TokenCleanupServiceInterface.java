package org.example.mobilyecommerce.service;

public interface TokenCleanupServiceInterface {

    void markExpiredTokens();

    void deleteExpiredTokens();

    void cleanupNow();
}
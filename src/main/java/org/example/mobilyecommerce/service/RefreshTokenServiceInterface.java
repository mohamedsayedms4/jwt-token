package org.example.mobilyecommerce.service;

import org.example.mobilyecommerce.model.RefreshToken;
import org.example.mobilyecommerce.model.User;

public interface RefreshTokenServiceInterface {

    RefreshToken createRefreshToken(User user);

    RefreshToken getByToken(String token);

    void deleteByUser(User user);

    boolean isValid(String token);

    int markExpiredTokens();

    int deleteExpiredTokens();
}
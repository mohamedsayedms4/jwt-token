package org.example.mobilyecommerce.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.mobilyecommerce.config.iwt.TokenHandler;
import org.example.mobilyecommerce.controller.vm.AuthRequestVm;
import org.example.mobilyecommerce.controller.vm.AuthResponseVm;
import org.example.mobilyecommerce.model.RefreshToken;
import org.example.mobilyecommerce.model.Token;
import org.example.mobilyecommerce.model.User;
import org.example.mobilyecommerce.repository.RefreshTokenRepository;
import org.example.mobilyecommerce.repository.TokenRepository;
import org.example.mobilyecommerce.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHandler tokenHandler;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       TokenRepository tokenRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       TokenHandler tokenHandler,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenHandler = tokenHandler;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Signup a new user and issue access & refresh tokens
     */
    @Transactional
    public AuthResponseVm signup(User user, String ip, String agent) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        String accessToken = tokenHandler.createAccessToken(savedUser);
        RefreshToken refreshToken = createRefreshToken(savedUser);

        saveUserToken(savedUser, accessToken, false, false, ip, agent);

        log.info("✅ New user created: {} from IP: {}", savedUser.getUsername(), ip);
        return new AuthResponseVm(accessToken, refreshToken.getToken());
    }

    /**
     * Login user and issue new tokens (without deleting old access tokens)
     */
    @Transactional
    public AuthResponseVm login(AuthRequestVm login, String ip, String agent) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        login.getUsername(),
                        login.getPassword()
                )
        );

        User user = userRepository.findByUsername(login.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate new access token & refresh token
        String accessToken = tokenHandler.createAccessToken(user);
        RefreshToken refreshToken = createRefreshToken(user);

        saveUserToken(user, accessToken, false, false, ip, agent);

        log.info("✅ User logged in successfully: {} from IP: {}", user.getUsername(), ip);
        return new AuthResponseVm(accessToken, refreshToken.getToken());
    }

    /**
     * Refresh access token using a valid refresh token
     */
    @Transactional
    public AuthResponseVm refresh(String refreshTokenValue) {
        // Find refresh token in DB
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        // Check if refresh token is expired
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            log.warn("⚠️ Attempted use of expired refresh token: {}", refreshTokenValue);
            throw new RuntimeException("Refresh token expired");
        }

        User user = refreshToken.getUser();
        if (user == null) {
            log.error("🚫 Refresh token has no associated user");
            throw new RuntimeException("User not found for this refresh token");
        }

        // Revoke old access tokens without deleting refresh tokens
        revokeAllUserTokens(user);
        log.debug("🗑️ All previous access tokens revoked for user: {}", user.getUsername());

        // Issue new access token
        String newAccessToken = tokenHandler.createAccessToken(user);

        // Save new access token
        saveUserToken(user, newAccessToken, false, false, "unknown-device", "0.0.0.0");

        log.info("🔄 New access token issued for user: {}", user.getUsername());
        return new AuthResponseVm(newAccessToken, refreshTokenValue);
    }

    /**
     * Logout user by deleting all access & refresh tokens
     */
    @Transactional
    public void logout(User user) {
        deleteAllUserTokens(user);
        log.info("✅ User logged out successfully: {}", user.getUsername());
    }

    // ----------------- Internal helper methods -----------------

    private void saveUserToken(User user, String jwtToken, boolean expired, boolean revoked, String ip, String agent) {
        Token token = Token.builder()
                .token(jwtToken)
                .user(user)
                .expired(expired)
                .revoked(revoked)
                .ipAddress(ip)
                .userAgent(agent)
                .build();
        tokenRepository.save(token);
        log.debug("💾 New token saved for user: {} from IP: {}", user.getUsername(), ip);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty()) return;

        validUserTokens.forEach(t -> {
            t.setExpired(true);
            t.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
        log.debug("🔒 Revoked {} access tokens for user: {}", validUserTokens.size(), user.getUsername());
    }

    private void deleteAllUserTokens(User user) {
        refreshTokenRepository.deleteByUserId(user.getId());
        log.debug("🗑️ Deleted refresh tokens for user: {}", user.getUsername());
        tokenRepository.deleteAllByUserId(user.getId());
        log.debug("🗑️ Deleted all access tokens for user: {}", user.getUsername());
    }

    /**
     * Create a new refresh token (1-minute expiry)
     */
    private RefreshToken createRefreshToken(User user) {
        String tokenValue = generateReadableToken();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(tokenValue)
                .expiryDate(Instant.now().plus(1, ChronoUnit.MINUTES)) // 1-minute expiry
                .expired(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Generate a human-readable token in format XXXX-XXXX-XXXX-XXXX
     */
    private String generateReadableToken() {
        SecureRandom random = new SecureRandom();
        StringBuilder token = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_+=/";
        for (int i = 0; i < 4; i++) {
            if (i > 0) token.append("-");
            for (int j = 0; j < 4; j++) {
                token.append(chars.charAt(random.nextInt(chars.length())));
            }
        }
        return token.toString();
    }
}

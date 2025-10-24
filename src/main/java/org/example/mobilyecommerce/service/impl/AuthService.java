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

        log.info("✅ تم إنشاء حساب جديد للمستخدم: {} من IP: {}", savedUser.getUsername(), ip);
        return new AuthResponseVm(accessToken, refreshToken.getToken());
    }

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

        // ❌ تعليق أو حذف حذف التوكنات القديمة
        // tokenRepository.deleteAllByUserId(user.getId());
        // log.debug("🗑️ تم حذف access tokens القديمة للمستخدم: {}", user.getUsername());

        String accessToken = tokenHandler.createAccessToken(user);
        RefreshToken refreshToken = createRefreshToken(user); // إنشاء refresh token جديد

        saveUserToken(user, accessToken, false, false, ip, agent);

        log.info("✅ تسجيل دخول ناجح للمستخدم: {} من IP: {}", user.getUsername(), ip);
        return new AuthResponseVm(accessToken, refreshToken.getToken());
    }


    /**
     * ✅ تجديد التوكن باستخدام الـ refresh token
     */
    @Transactional
    public AuthResponseVm refresh(String refreshTokenValue) {
        // 🔍 البحث عن الـ Refresh Token في قاعدة البيانات
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        // ⏰ التحقق من صلاحية الـ Refresh Token
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            log.warn("⚠️ حاول المستخدم استخدام Refresh Token منتهي الصلاحية: {}", refreshTokenValue);
            throw new RuntimeException("Refresh token expired");
        }

        User user = refreshToken.getUser();
        if (user == null) {
            log.error("🚫 الـ Refresh Token لا يحتوي على مستخدم مرتبط");
            throw new RuntimeException("User not found for this refresh token");
        }

        // ❌ إلغاء جميع الـ access tokens القديمة فقط (دون حذف الـ refresh)
        revokeAllUserTokens(user);
        log.debug("🗑️ تم تعطيل جميع الـ access tokens القديمة للمستخدم: {}", user.getUsername());

        // 🎟️ إنشاء Access Token جديد
        String newAccessToken = tokenHandler.createAccessToken(user);

        // 💾 حفظ الـ access token الجديد (مع قيم افتراضية للجهاز والعنوان IP)
        saveUserToken(user, newAccessToken, false, false, "unknown-device", "0.0.0.0");

        log.info("🔄 تم إصدار Access Token جديد للمستخدم: {}", user.getUsername());
        return new AuthResponseVm(newAccessToken, refreshTokenValue);
    }

    /**
     * ✅ تسجيل الخروج - حذف كل التوكنات
     */
    @Transactional
    public void logout(User user) {
        deleteAllUserTokens(user);
        log.info("✅ تسجيل خروج ناجح للمستخدم: {}", user.getUsername());
    }

    // ----------------- دوال داخلية -----------------

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
        log.debug("💾 تم حفظ توكن جديد للمستخدم: {} من IP: {}", user.getUsername(), ip);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty()) return;

        validUserTokens.forEach(t -> {
            t.setExpired(true);
            t.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
        log.debug("🔒 تم تعطيل {} توكن للمستخدم: {}", validUserTokens.size(), user.getUsername());
    }

    private void deleteAllUserTokens(User user) {
        refreshTokenRepository.deleteByUserId(user.getId());
        log.debug("🗑️ تم حذف refresh token للمستخدم: {}", user.getUsername());
        tokenRepository.deleteAllByUserId(user.getId());
        log.debug("🗑️ تم حذف جميع access tokens للمستخدم: {}", user.getUsername());
    }

    /**
     * ✅ إنشاء Refresh Token جديد (دقيقة واحدة صلاحية)
     */
    private RefreshToken createRefreshToken(User user) {
        String tokenValue = generateReadableToken();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(tokenValue)
                .expiryDate(Instant.now().plus(1, ChronoUnit.MINUTES)) // ⏱️ دقيقة واحدة
                .expired(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    private String generateReadableToken() {
        SecureRandom random = new SecureRandom();
        StringBuilder token = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < 4; i++) {
            if (i > 0) token.append("-");
            for (int j = 0; j < 4; j++) {
                token.append(chars.charAt(random.nextInt(chars.length())));
            }
        }
        return token.toString();
    }
}

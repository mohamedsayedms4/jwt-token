    package org.example.mobilyecommerce.service.impl;

    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.example.mobilyecommerce.config.iwt.TokenHandler;
    import org.example.mobilyecommerce.controller.vm.AuthRequestVm;
    import org.example.mobilyecommerce.controller.vm.AuthResponseVm;
    import org.example.mobilyecommerce.exception.InvalidCredentialsException;
    import org.example.mobilyecommerce.exception.TokenExpiredException;
    import org.example.mobilyecommerce.exception.UserAlreadyExistsException;
    import org.example.mobilyecommerce.exception.UserNotFoundException;
    import org.example.mobilyecommerce.model.RefreshToken;
    import org.example.mobilyecommerce.model.Token;
    import org.example.mobilyecommerce.model.User;
    import org.example.mobilyecommerce.repository.TokenRepository;
    import org.example.mobilyecommerce.service.AuthServiceInterface;
    import org.example.mobilyecommerce.service.RefreshTokenServiceInterface;
    import org.example.mobilyecommerce.repository.UserRepository;
    import org.springframework.security.authentication.AuthenticationManager;
    import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.time.Instant;
    import java.util.Optional;

    @Service
    @RequiredArgsConstructor
    @Slf4j
    public class AuthService implements AuthServiceInterface {

        private final UserRepository userRepository;
        private final TokenRepository tokenRepository;
        private final RefreshTokenServiceInterface refreshTokenService;
        private final TokenHandler tokenHandler;
        private final PasswordEncoder passwordEncoder;
        private final AuthenticationManager authenticationManager;

        @Override
        @Transactional
        public AuthResponseVm signup(User user, String ip, String agent) {
            log.info("🔐 Signup attempt for username: {} from IP: {}", user.getEmail(), ip);

            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                log.warn("⚠️ Username already exists: {}", user.getUsername());
                throw new UserAlreadyExistsException("Username already exists: " + user.getUsername());
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User savedUser = userRepository.save(user);

            String accessToken = tokenHandler.createAccessToken(savedUser);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser);

            saveAccessToken(savedUser, accessToken, ip, agent);

            log.info("✅ New user created: {} from IP: {}", savedUser.getUsername(), ip);
            return new AuthResponseVm(accessToken, refreshToken.getToken());
        }

    //    @Override
    //    @Transactional
    //    public AuthResponseVm login(AuthRequestVm login, String ip, String agent) {
    //        log.info("🔐 Login attempt for username: {} from IP: {}", login.getEmail(), ip);
    //
    //        authenticationManager.authenticate(
    //                new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPassword())
    //        );
    //
    //
    //        User user = userRepository.findByUsername(login.getEmail())
    //                .orElseThrow(() -> new InvalidCredentialsException("User not found: " + login.getEmail()));
    //
    //        String accessToken = tokenHandler.createAccessToken(user);
    //        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
    //
    //        saveAccessToken(user, accessToken, ip, agent);
    //
    //        log.info("✅ User logged in successfully: {} from IP: {}", user.getUsername(), ip);
    //        return new AuthResponseVm(accessToken, refreshToken.getToken());
    //    }

        @Override
        @Transactional
        public AuthResponseVm login(AuthRequestVm login, String ip, String agent) {
            log.info("🔐 Login attempt for username: {} from IP: {}", login.getEmail(), ip);

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPassword())
            );

            // ✅ التعديل: استخدام findByEmail بدلاً من findByUsername
            User user = userRepository.findByEmail(login.getEmail())
                    .orElseThrow(() -> new InvalidCredentialsException("User not found: " + login.getEmail()));

            String accessToken = tokenHandler.createAccessToken(user);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            saveAccessToken(user, accessToken, ip, agent);

            log.info("✅ User logged in successfully: {} from IP: {}", user.getUsername(), ip);
            return new AuthResponseVm(accessToken, refreshToken.getToken());
        }

        @Override
        @Transactional
        public AuthResponseVm refresh(String refreshTokenValue, String ip, String agent) {
            log.info("🔄 Refresh token request from IP: {}", ip);

            if (!refreshTokenService.isValid(refreshTokenValue)) {
                log.warn("⚠️ Invalid or expired refresh token");
                throw new TokenExpiredException("Refresh token is invalid or expired");
            }

            RefreshToken refreshToken = refreshTokenService.getByToken(refreshTokenValue);
            User user = refreshToken.getUser();

            revokeAllUserAccessTokens(user);

            String newAccessToken = tokenHandler.createAccessToken(user);
            saveAccessToken(user, newAccessToken, ip, agent);

            log.info("🔄 New access token issued for user: {} from IP: {}", user.getUsername(), ip);
            return new AuthResponseVm(newAccessToken, refreshTokenValue);
        }

        @Override
        @Transactional
        public Boolean resetPassword(String username, String newPassword) {
            // 1️⃣ التحقق من وجود المستخدم
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("User with email " + username + " not found"));

            // 2️⃣ منع إعادة استخدام نفس كلمة السر القديمة
            if (passwordEncoder.matches(newPassword, user.getPassword())) {
                throw new IllegalArgumentException("New password must be different from the old one");
            }

            // 3️⃣ تشفير كلمة السر الجديدة وتحديثها مباشرة
            user.setPassword(passwordEncoder.encode(newPassword));

            // 4️⃣ حفظ التعديل (بسبب @Transactional يكفي التعديل على الكائن فقط)
            // لا تحتاج إلى userRepository.save(user) هنا إلا لو عندك detached entity

            return true;
        }

        @Override
        @Transactional  // ✅ مهم جداً!
        public void logout(User user) {
            log.info("🚪 Logout request for user: {}", user.getUsername());

            try {
                // 1. إلغاء Access Tokens
                revokeAllUserAccessTokens(user);
                log.debug("✅ Revoked all access tokens");

                // 2. حذف Refresh Tokens
                refreshTokenService.deleteByUser(user);
                log.debug("✅ Deleted all refresh tokens");

                log.info("✅ User logged out successfully: {}", user.getUsername());

            } catch (Exception e) {
                log.error("❌ Error during logout: {}", e.getMessage(), e);
                throw new RuntimeException("Logout failed: " + e.getMessage());
            }
        }
        // ---------------- Internal helpers ----------------

        /**
         * حفظ Access Token في قاعدة البيانات
         */
        private void saveAccessToken(User user, String jwtToken, String ip, String agent) {
            Instant now = Instant.now();
            Token token = Token.builder()
                    .token(jwtToken)
                    .user(user)
                    .expired(false)
                    .revoked(false)
                    .ipAddress(ip)
                    .userAgent(agent)
                    .createdAt(now)
                    .expiryDate(now.plus(tokenHandler.getAccessTime()))
                    .build();
            tokenRepository.save(token);
            log.debug("💾 Access token saved for user: {} with IP: {}", user.getUsername(), ip);
        }

        /**
         * إلغاء جميع الـ Access Tokens النشطة للمستخدم
         */
        private void revokeAllUserAccessTokens(User user) {
            var tokens = tokenRepository.findAllValidTokenByUser(user.getId());
            if (!tokens.isEmpty()) {
                tokens.forEach(t -> {
                    t.setExpired(true);
                    t.setRevoked(true);
                });
                tokenRepository.saveAll(tokens);
                log.debug("🔒 Revoked {} access tokens for user: {}", tokens.size(), user.getUsername());
            }
        }
    }
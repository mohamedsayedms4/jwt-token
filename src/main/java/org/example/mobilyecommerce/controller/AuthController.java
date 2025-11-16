package org.example.mobilyecommerce.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mobilyecommerce.controller.vm.AuthRequestVm;
import org.example.mobilyecommerce.controller.vm.AuthResponseVm;
import org.example.mobilyecommerce.controller.vm.ResetPasswordRequest;
import org.example.mobilyecommerce.dto.UserDto;
import org.example.mobilyecommerce.mapper.UserMapper;
import org.example.mobilyecommerce.model.User;
import org.example.mobilyecommerce.service.impl.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;
    /**
     * ✅ استخراج IP الحقيقي للمستخدم من Headers (للتعامل مع Reverse Proxy)
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // في حالة وجود عدة IPs (proxy chain)، خذ الأول
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        log.debug("🌐 Extracted client IP: {}", ip);
        return ip;
    }

    /**
     * ✅ تسجيل الدخول - يرجع Access و Refresh Token مع تخزين IP و Agent
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseVm> login(@RequestBody AuthRequestVm login, HttpServletRequest request) {
        String ip = getClientIp(request);
        String agent = request.getHeader("User-Agent");

        log.info("🔑 Login request from IP: {} for user: {}", ip, login.getEmail());

        AuthResponseVm response = authService.login(login, ip, agent);
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ إنشاء حساب جديد - مع تسجيل IP و Agent
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponseVm> signup(@RequestBody @Valid UserDto account, HttpServletRequest request) {
        String ip = getClientIp(request);
        String agent = request.getHeader("User-Agent");

        log.info("📝 Signup request from IP: {} for user: {}", ip, account.getUsername());
        User user = userMapper.toEntity(account);
        AuthResponseVm response = authService.signup(user, ip, agent);
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ endpoint محمي لاختبار الـ JWT
     */
    @GetMapping("/hello")
    public ResponseEntity<Map<String, String>> hello(
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {

        String ip = getClientIp(request);

        log.info("👋 Hello request from user: {} (IP: {})",
                user != null ? user.getUsername() : "unknown", ip);

        return ResponseEntity.ok(Map.of(
                "message", "Hello, " + (user != null ? user.getUsername() : "Guest") + "!",
                "ip", ip,
                "status", "secured"
        ));
    }

    /**
     * ✅ تجديد الـ Access Token باستخدام الـ Refresh Token
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseVm> refresh(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {

        String refreshToken = body.get("refreshToken");
        String ip = getClientIp(request);
        String agent = request.getHeader("User-Agent");

        log.info("🔄 Refresh token request from IP: {}", ip);

        AuthResponseVm response = authService.refresh(refreshToken, ip, agent);
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ تسجيل الخروج وإلغاء جميع التوكينات
     * يستخدم @AuthenticationPrincipal للحصول على المستخدم الحالي من الـ JWT
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@AuthenticationPrincipal User user) {
        log.info("🚪 Logout request for user: {}", user.getUsername());

        authService.logout(user);

        return ResponseEntity.ok(Map.of(
                "message", "Logged out successfully",
                "username", user.getUsername()
        ));
    }

    /**
     * ✅ معلومات عن التوكنات (للتشخيص والاختبار)
     */
    @GetMapping("/token-info")
    public ResponseEntity<Map<String, String>> tokenInfo() {
        return ResponseEntity.ok(Map.of(
                "accessTokenDuration", "30 minutes",
                "refreshTokenDuration", "10 days",
                "note", "Access token expires after 30 minutes, use refresh token to get a new one"
        ));
    }

    /**
     * ✅ اختبار IP - للتأكد من أن IP يتم استخراجه بشكل صحيح
     */
    @GetMapping("/test-ip")
    public ResponseEntity<Map<String, String>> testIp(HttpServletRequest request) {
        String ip = getClientIp(request);
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        String xRealIp = request.getHeader("X-Real-IP");
        String remoteAddr = request.getRemoteAddr();

        log.info("🧪 IP Test - Final: {}, X-Forwarded-For: {}, X-Real-IP: {}, Remote: {}",
                ip, xForwardedFor, xRealIp, remoteAddr);

        return ResponseEntity.ok(Map.of(
                "extractedIp", ip != null ? ip : "null",
                "xForwardedFor", xForwardedFor != null ? xForwardedFor : "null",
                "xRealIp", xRealIp != null ? xRealIp : "null",
                "remoteAddr", remoteAddr != null ? remoteAddr : "null"
        ));
    }


    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        boolean success = authService.resetPassword(request.getUsername(), request.getNewPassword());

        if (success) {
            return ResponseEntity.ok("Password reset successfully.");
        } else {
            return ResponseEntity.badRequest().body("Failed to reset password.");
        }
    }
}
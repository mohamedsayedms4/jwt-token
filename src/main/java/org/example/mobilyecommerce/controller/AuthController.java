package org.example.mobilyecommerce.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.mobilyecommerce.controller.vm.AuthRequestVm;
import org.example.mobilyecommerce.controller.vm.AuthResponseVm;
import org.example.mobilyecommerce.model.User;
import org.example.mobilyecommerce.repository.UserRepository;
import org.example.mobilyecommerce.service.impl.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    /**
     * ✅ تسجيل الدخول - يرجع Access و Refresh Token مع تخزين IP و Agent
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseVm> login(@RequestBody AuthRequestVm login, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String agent = request.getHeader("User-Agent");

        AuthResponseVm response = authService.login(login, ip, agent);
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ إنشاء حساب جديد - مع تسجيل IP و Agent
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponseVm> signup(@RequestBody User account, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String agent = request.getHeader("User-Agent");

        AuthResponseVm response = authService.signup(account, ip, agent);
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ endpoint محمي لاختبار الـ JWT
     */
    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello, secured world!");
    }

    /**
     * ✅ تجديد الـ Access Token باستخدام الـ Refresh Token
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseVm> refresh(@RequestParam String refreshToken , HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String agent = request.getHeader("User-Agent");
        AuthResponseVm response = authService.refresh(refreshToken,ip, agent);
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ تسجيل الخروج وإلغاء جميع التوكينات
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        authService.logout(user);
        return ResponseEntity.ok("Logged out successfully");
    }
}

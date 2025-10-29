package org.example.mobilyecommerce.config.iwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.example.mobilyecommerce.model.Role;
import org.example.mobilyecommerce.model.Token;
import org.example.mobilyecommerce.model.User;
import org.example.mobilyecommerce.repository.TokenRepository;
import org.example.mobilyecommerce.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.*;

@Service
@Getter
public class TokenHandler {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final String secret;
    private final Duration accessTime;
    private final Duration refreshTime;
    private final Key key;
    private final JwtParser jwtParser;

    public TokenHandler(JwtToken jwtToken, UserRepository userRepository, TokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.secret = jwtToken.getSecret();
        this.accessTime = jwtToken.getAccessTime();
        this.refreshTime = jwtToken.getRefreshTime();

        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.jwtParser = Jwts.parserBuilder().setSigningKey(key).build();
    }

    /**
     * إنشاء Access Token
     */
    public String createAccessToken(User user) {
        Date issueDate = new Date();
        Date expirationDate = Date.from(issueDate.toInstant().plus(accessTime));

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("username", user.getUsername());
        claims.put("roles", user.getRoles().stream().map(Role::getRole).toList());
        claims.put("type", "ACCESS");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject("Mobily.cloud")
                .setIssuedAt(issueDate)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();
    }

    /**
     * التحقق من صحة Token
     *
     * @param token التوكن المرسل
     * @param requestIp عنوان IP للطلب الحالي
     * @param requestAgent User-Agent للطلب الحالي
     * @return User object إذا كان التوكن صحيح، null إذا كان غير صحيح
     */
    public User checkToken(String token, String requestIp, String requestAgent) {
        try {
            Claims claims = jwtParser.parseClaimsJws(token).getBody();
            String username = claims.get("username", String.class);
            String type = claims.get("type", String.class);

            // التحقق من نوع التوكن
            if (!"ACCESS".equals(type)) {
                log.warn("⚠️ Invalid token type: {}", type);
                return null;
            }

            // البحث عن التوكن في قاعدة البيانات
            Optional<Token> tokenEntity = tokenRepository.findByToken(token);
            if (tokenEntity.isEmpty()) {
                log.warn("⚠️ Token not found in database");
                return null;
            }

            Token t = tokenEntity.get();

            // التحقق من حالة التوكن
            if (t.isExpired() || t.isRevoked()) {
                log.warn("⚠️ Token is expired or revoked");
                return null;
            }

            // تسجيل معلومات التشخيص
            log.debug("🔍 Token validation - Stored IP: {}, Request IP: {}", t.getIpAddress(), requestIp);
            log.debug("🔍 Token validation - Stored Agent: {}, Request Agent: {}", t.getUserAgent(), requestAgent);

            // ⚠️ التحقق من IP و User-Agent (اختياري - يمكن تفعيله للأمان الإضافي)
            // ملاحظة: في بيئة production مع proxies متعددة، قد يكون من الأفضل استخدام آلية أخرى للتحقق

            // إذا أردت تفعيل التحقق الصارم، أزل التعليق من الكود التالي:
            /*
            if (!Objects.equals(t.getIpAddress(), requestIp)) {
                log.warn("⚠️ IP mismatch - stored: {}, request: {}", t.getIpAddress(), requestIp);
                throw new RuntimeException("Access denied: IP address mismatch");
            }

            if (!Objects.equals(t.getUserAgent(), requestAgent)) {
                log.warn("⚠️ User-Agent mismatch - stored: {}, request: {}",
                    t.getUserAgent() != null ? t.getUserAgent().substring(0, Math.min(50, t.getUserAgent().length())) : "null",
                    requestAgent != null ? requestAgent.substring(0, Math.min(50, requestAgent.length())) : "null");
                throw new RuntimeException("Access denied: User-Agent mismatch");
            }
            */

            // جلب المستخدم من قاعدة البيانات
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        } catch (ExpiredJwtException e) {
            log.warn("⚠️ JWT token expired: {}", e.getMessage());
            return null;
        } catch (MalformedJwtException e) {
            log.error("🔴 Malformed JWT token: {}", e.getMessage());
            return null;
        } catch (UnsupportedJwtException e) {
            log.error("🔴 Unsupported JWT token: {}", e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            log.error("🔴 JWT claims string is empty: {}", e.getMessage());
            return null;
        } catch (JwtException e) {
            log.error("🔴 JWT parsing error: {}", e.getMessage());
            return null;
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TokenHandler.class);
}
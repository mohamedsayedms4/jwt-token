package org.example.mobilyecommerce.config.iwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
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

    // ✅ إنشاء access token
    public String createAccessToken(User user) {
        Date issueDate = new Date();
        Date expirationDate = Date.from(issueDate.toInstant().plus(accessTime));

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("username", user.getUsername());
        claims.put("roles", user.getRoles().stream().map(Role::getRole).toList());
        claims.put("type", "ACCESS"); // ✅ إضافة type

        return Jwts.builder()
                .setClaims(claims)
                .setSubject("Mobily.cloud")
                .setIssuedAt(issueDate)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();
    }

    // ✅ إنشاء refresh token
    public String createRefreshToken(User user) {
        return createToken(user, refreshTime, "REFRESH");
    }

    private String createToken(User user, Duration duration, String type) {
        Date issueDate = new Date();
        Date expirationDate = Date.from(issueDate.toInstant().plus(duration));

        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("type", type);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject("Mobily.cloud")
                .setIssuedAt(issueDate)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();
    }

    public User checkToken(String token, String requestIp, String requestAgent) {
        try {
            Claims claims = jwtParser.parseClaimsJws(token).getBody();
            String username = claims.get("username", String.class);
            String type = claims.get("type", String.class);

            if (!"ACCESS".equals(type)) return null;

            Optional<Token> tokenEntity = tokenRepository.findByToken(token);
            if (tokenEntity.isEmpty() || tokenEntity.get().isExpired() || tokenEntity.get().isRevoked()) {
                return null;
            }


            Token t = tokenEntity.get();
            if (!Objects.equals(t.getIpAddress(), requestIp) ||
                    !Objects.equals(t.getUserAgent(), requestAgent)) {
                // 🚫 الجهاز/المتصفح غير معروف
                throw new RuntimeException("Access denied: unknown device");
            }

            // ✅ تحقق من IP و User-Agent
            if (!t.getIpAddress().equals(requestIp) || !t.getUserAgent().equals(requestAgent)) {
                return null;
            }


            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        } catch (JwtException e) {
            return null;
        }
    }


    // ✅ تحقق من Refresh Token
    public User checkRefreshToken(String token) {
        try {
            Claims claims = jwtParser.parseClaimsJws(token).getBody();
            String username = claims.get("username", String.class);
            String type = claims.get("type", String.class);

            if (!"REFRESH".equals(type)) return null;

            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        } catch (JwtException e) {
            return null;
        }
    }
}
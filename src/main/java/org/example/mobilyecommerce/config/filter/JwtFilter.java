package org.example.mobilyecommerce.config.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mobilyecommerce.config.iwt.TokenHandler;
import org.example.mobilyecommerce.model.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final TokenHandler tokenHandler;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        log.debug("🟦 Incoming request: {} {}", request.getMethod(), path);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("⚪ No Bearer token found — skipping authentication for path {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();
        log.debug("🟨 Extracted token: {}", token.isEmpty() ? "[EMPTY]" : "[REDACTED]");

        User user;

        try {
            user = tokenHandler.checkToken(token, request.getRemoteAddr(), request.getHeader("User-Agent"));

            if (user == null) {
                log.warn("🔴 Token validation failed — possibly invalid or from unknown device. IP: {}, Path: {}",
                        request.getRemoteAddr(), path);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Access denied: unknown device or mismatched token");
                return;
            }

            log.info("🟢 Token validated successfully for user '{}' (IP: {})", user.getUsername(), request.getRemoteAddr());

        } catch (RuntimeException e) {
            log.error("🔴 Token validation error: {} (Path: {})", e.getMessage(), path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(e.getMessage());
            return;
        }

        // Map roles to authorities for Spring Security
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRole().toUpperCase()))
                .collect(Collectors.toList());

        log.debug("🟪 Mapped roles to authorities for user {}: {}", user.getUsername(), authorities);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("🟩 SecurityContext updated successfully for user {}", user.getUsername());

        filterChain.doFilter(request, response);
        log.debug("✅ Request {} completed for user {}", path, user.getUsername());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        boolean skip = path.contains("/api/auth/login") || path.contains("/api/auth/signup");
        if (skip) {
            log.debug("⚫ Skipping JwtFilter for path {}", path);
        }
        return skip;
    }
}

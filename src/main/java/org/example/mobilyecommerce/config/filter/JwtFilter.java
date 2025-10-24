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

        String authHeader = request.getHeader("Authorization");

        // If no token or not Bearer type, skip authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No Bearer token found, skipping authentication for path {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();
        User user;

        try {
            // Validate the token along with client IP and User-Agent
            user = tokenHandler.checkToken(token, request.getRemoteAddr(), request.getHeader("User-Agent"));

            if (user == null) {
                log.warn("Token validation failed: unknown device or mismatched token for IP {} and path {}",
                        request.getRemoteAddr(), request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Access denied: unknown device or mismatched token");
                return;
            }

            log.debug("Token validated successfully for user {} on path {}", user.getUsername(), request.getRequestURI());

        } catch (RuntimeException e) {
            log.error("Token validation error: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(e.getMessage());
            return;
        }

        // Map roles to authorities for Spring Security
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRole().toUpperCase()))
                .collect(Collectors.toList());

        // Create authentication object and set it in the security context
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("Authentication set in SecurityContext for user {}", user.getUsername());

        // Continue with the remaining filters
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip filter for login and signup endpoints
        String path = request.getRequestURI();
        boolean skip = path.contains("/api/auth/login") || path.contains("/api/auth/signup");
        if (skip) {
            log.debug("Skipping JwtFilter for path {}", path);
        }
        return skip;
    }
}

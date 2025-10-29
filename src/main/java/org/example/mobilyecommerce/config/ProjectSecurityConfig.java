package org.example.mobilyecommerce.config;

import lombok.RequiredArgsConstructor;
import org.example.mobilyecommerce.config.filter.Bucket4jRateLimitFilter;
import org.example.mobilyecommerce.config.filter.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * ✅ Secure Spring Security Configuration
 * - JWT authentication (stateless)
 * - Strict CORS for production
 * - Rate limiting (Bucket4j)
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class ProjectSecurityConfig {

    private final JwtFilter jwtFilter;
    private final Bucket4jRateLimitFilter bucket4jRateLimitFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // No session — JWT-based
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Disable CSRF (for APIs)
                .csrf(csrf -> csrf.disable())

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/hello").authenticated()
                        .requestMatchers("/api/auth/login", "/api/auth/signup", "/api/auth/refresh").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().permitAll()
                )

                // ✅ Use our custom CORS configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Filters
                .addFilterBefore(bucket4jRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                // Disable default login forms
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable());

        return http.build();
    }

    /**
     * ✅ Secure CORS Configuration for bigzero.online
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // ✅ Allowed Origins (Production + Local)
        config.setAllowedOrigins(List.of(
                "https://bigzero.online",
                "https://www.bigzero.online",
                "http://localhost:3000" // for local testing only
        ));

        // ✅ Only allow safe methods
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // ✅ Allow headers for JWT tokens and custom requests
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "Origin"));

        // ✅ Allow credentials (for cookies or Authorization headers)
        config.setAllowCredentials(true);

        // ✅ Cache preflight responses for 1 hour
        config.setMaxAge(3600L);

        // Apply to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

package org.example.mobilyecommerce.config.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class Bucket4jRateLimitFilter extends OncePerRequestFilter {

    // Thread-safe map to cache buckets for each client+path combination
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    /**
     * Create a new rate limit bucket based on the API path.
     * Login/signup endpoints have stricter limits.
     */
    private Bucket createNewBucket(String path) {
        Bandwidth limit;
        if (path.startsWith("/api/auth/login") || path.startsWith("/api/auth/signup")) {
            // Stricter limit for authentication endpoints
            Refill refill = Refill.greedy(3, Duration.ofMinutes(10));
            limit = Bandwidth.classic(3, refill);
            log.info("Created login/signup bucket: limit=3 requests per 10 minutes for path {}", path);
        } else {
            // General API endpoints
            Refill refill = Refill.greedy(60, Duration.ofMinutes(15));
            limit = Bandwidth.classic(60, refill);
            log.info("Created general bucket: limit=60 requests per 15 minutes for path {}", path);
        }
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Resolve the bucket for a given client and path.
     * If it does not exist, create a new one and cache it.
     */
    private Bucket resolveBucket(String key, String path) {
        return cache.computeIfAbsent(key + ":" + path, k -> {
            log.info("Creating new bucket for key: {}", k);
            return createNewBucket(path);
        });
    }

    /**
     * Generate a unique client key based on IP and User-Agent.
     */
    private String getClientKey(HttpServletRequest req) {
        // Check X-Forwarded-For header for client IP (useful behind proxies)
        String xfwd = req.getHeader("X-Forwarded-For");
        String ip = (xfwd != null) ? xfwd.split(",")[0].trim() : req.getRemoteAddr();

        // Get User-Agent, fallback to "unknown-agent"
        String agent = req.getHeader("User-Agent");
        String key = ip + "|" + (agent != null ? agent : "unknown-agent");

        log.debug("Generated client key: {}", key);
        return key;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String key = getClientKey(request);
        String path = request.getRequestURI();

        // Retrieve or create bucket for this client/path
        Bucket bucket = resolveBucket(key, path);

        // Try consuming 1 token from the bucket
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // Request allowed, add remaining tokens in header
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            log.debug("Request allowed for key {} on path {} - Remaining tokens: {}", key, path, probe.getRemainingTokens());
            filterChain.doFilter(request, response);
        } else {
            // Request denied, set 429 status and Retry-After header
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(waitForRefill));
            response.getWriter().write("Too many requests");
            log.warn("Rate limit exceeded for key {} on path {} - Retry after {} seconds", key, path, waitForRefill);
        }
    }
}

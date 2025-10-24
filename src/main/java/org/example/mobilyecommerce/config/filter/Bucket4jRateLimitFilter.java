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

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    private Bucket createNewBucket(String path) {
        Bandwidth limit;
        if (path.startsWith("/api/auth/login")||path.startsWith("/api/auth/signup")) {
            // صارم للـ login
            Refill refill = Refill.greedy(3, Duration.ofMinutes(10));
            limit = Bandwidth.classic(3, refill);
        } else {
            // باقي الـ API
            Refill refill = Refill.greedy(60, Duration.ofMinutes(15));
            limit = Bandwidth.classic(60, refill);
        }
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket resolveBucket(String key, String path) {
        return cache.computeIfAbsent(key + ":" + path, k -> {
            log.info("Creating new bucket for key: {}", k);
            return createNewBucket(path);
        });
    }

    private String getClientKey(HttpServletRequest req) {
        String xfwd = req.getHeader("X-Forwarded-For");
        String ip = (xfwd != null) ? xfwd.split(",")[0].trim() : req.getRemoteAddr();
        String agent = req.getHeader("User-Agent");
        return ip + "|" + (agent != null ? agent : "unknown-agent");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String key = getClientKey(request);
        String path = request.getRequestURI();
        Bucket bucket = resolveBucket(key, path);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(waitForRefill));
            response.getWriter().write("Too many requests");
            log.warn("Rate limit exceeded for key {} - Retry after {} seconds", key, waitForRefill);
        }
    }
}

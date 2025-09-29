package com.roze.nexacommerce.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {
    private final RateLimitService rateLimitService;
    private final RateLimitProperties rateLimitProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!rateLimitProperties.isEnabled()) {
            return true;
        }

        String clientKey = getClientKey(request);
        String endpoint = request.getRequestURI();
        String method = request.getMethod();

        String rateLimitKey = clientKey + ":" + method + ":" + endpoint;

        int capacity = getCapacityForEndpoint(endpoint);
        Duration duration = Duration.ofMinutes(rateLimitProperties.getRefillDuration());

        if (!rateLimitService.tryConsume(rateLimitKey, capacity, duration)) {
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("""
                {
                    "error": "Rate limit exceeded",
                    "message": "Too many requests. Please try again later.",
                    "code": 429
                }
                """);

            log.warn("Rate limit exceeded for client: {} on {} {}", clientKey, method, endpoint);
            return false;
        }

        // Add rate limit headers for information
        Long remaining = rateLimitService.getRemainingRequests(rateLimitKey, capacity);
        response.addHeader("X-RateLimit-Limit", String.valueOf(capacity));
        response.addHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        response.addHeader("X-RateLimit-Reset", String.valueOf(duration.toSeconds()));

        return true;
    }

    private String getClientKey(HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        String apiKey = request.getHeader("X-API-Key");

        // Check for X-Forwarded-For header if behind proxy
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            clientIp = xForwardedFor.split(",")[0].trim();
        }

        // Check for other common proxy headers
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            clientIp = realIp;
        }

        if (apiKey != null && !apiKey.trim().isEmpty()) {
            return "api_key:" + apiKey;
        }

        return "ip:" + clientIp;
    }

    private int getCapacityForEndpoint(String endpoint) {
        if (endpoint.contains("/auth/")) {
            return rateLimitProperties.getEndpoints().getAuth();
        } else if (endpoint.contains("/api/admin/") || endpoint.contains("/admin/")) {
            return rateLimitProperties.getEndpoints().getAdminApi();
        } else if (endpoint.contains("/api/user/") || endpoint.contains("/user/")) {
            return rateLimitProperties.getEndpoints().getUserApi();
        } else if (endpoint.contains("/api/public/") || endpoint.contains("/public/")) {
            return rateLimitProperties.getEndpoints().getPublicApi();
        }

        return rateLimitProperties.getCapacity();
    }
}
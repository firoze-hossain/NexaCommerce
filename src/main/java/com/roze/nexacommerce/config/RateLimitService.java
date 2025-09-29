package com.roze.nexacommerce.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Simple Redis-based rate limiting using INCR and EXPIRE
     */
    public boolean tryConsume(String key, int capacity, Duration duration) {
        String redisKey = "rate_limit:" + key;

        try {
            Long currentCount = redisTemplate.opsForValue().increment(redisKey);

            if (currentCount == null) {
                log.error("Redis increment returned null for key: {}", redisKey);
                return false;
            }

            if (currentCount == 1) {
                // Set expiration on first request
                redisTemplate.expire(redisKey, duration.toSeconds(), TimeUnit.SECONDS);
            }

            boolean allowed = currentCount <= capacity;

            if (!allowed) {
                log.debug("Rate limit exceeded for key: {}, current: {}, limit: {}",
                        redisKey, currentCount, capacity);
            }

            return allowed;

        } catch (Exception e) {
            log.error("Error in rate limiting for key: {}", redisKey, e);
            // Allow request if Redis fails (fail-open strategy)
            return true;
        }
    }

    /**
     * Get remaining requests for a key
     */
    public Long getRemainingRequests(String key, int capacity) {
        String redisKey = "rate_limit:" + key;
        try {
            String current = (String) redisTemplate.opsForValue().get(redisKey);
            if (current == null) {
                return (long) capacity;
            }
            Long currentCount = Long.parseLong(current);
            return Math.max(0, capacity - currentCount);
        } catch (Exception e) {
            log.error("Error getting remaining requests for key: {}", key, e);
            return (long) capacity;
        }
    }

    /**
     * Reset rate limit for a key
     */
    public void resetLimit(String key) {
        String redisKey = "rate_limit:" + key;
        try {
            redisTemplate.delete(redisKey);
        } catch (Exception e) {
            log.error("Error resetting rate limit for key: {}", key, e);
        }
    }
}
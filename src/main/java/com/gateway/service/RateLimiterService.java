package com.gateway.service;

import com.gateway.model.RateLimitResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

@Slf4j
@Service
public class RateLimiterService {

    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;

    @Autowired(required = false)
    private DefaultRedisScript<Long> rateLimitScript;

    @Value("${gateway.rate-limit.default-limit:100}")
    private int defaultLimit;

    @Value("${gateway.rate-limit.redis-key-prefix:gateway:ratelimit:}")
    private String keyPrefix;

    @Value("${gateway.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    /**
     * Check if request is allowed based on rate limit
     */
    public RateLimitResult checkRateLimit(String identifier) {
        return checkRateLimit(identifier, defaultLimit);
    }

    /**
     * Check if request is allowed with custom limit
     */
    public RateLimitResult checkRateLimit(String identifier, int limit) {

        // If rate limiting is disabled or Redis is not available
        if (!rateLimitEnabled || redisTemplate == null || rateLimitScript == null) {
            log.debug("Rate limiting disabled or Redis unavailable. Allowing request.");
            return RateLimitResult.failOpen();
        }

        String key = buildKey(identifier);

        try {
            Long remaining = redisTemplate.execute(
                    rateLimitScript,
                    Collections.singletonList(key),
                    String.valueOf(limit),
                    "60" // 60 seconds window
            );

            if (remaining != null && remaining >= 0) {
                log.debug("Rate limit check PASSED for {}: {} requests remaining",
                        identifier, remaining);
                return RateLimitResult.allowed(remaining);
            } else {
                log.warn("Rate limit EXCEEDED for {}", identifier);
                return RateLimitResult.exceeded("Rate limit exceeded");
            }

        } catch (Exception e) {
            log.error("Error checking rate limit for {}: {}. Failing open.",
                    identifier, e.getMessage());
            // Fail open - allow request if Redis error
            return RateLimitResult.failOpen();
        }
    }

    /**
     * Get current usage for identifier
     */
    public long getCurrentUsage(String identifier) {
        if (redisTemplate == null) {
            return 0;
        }

        String key = buildKey(identifier);
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) : 0;
    }

    /**
     * Reset rate limit for identifier
     */
    public void resetRateLimit(String identifier) {
        if (redisTemplate == null) {
            log.warn("Cannot reset rate limit - Redis unavailable");
            return;
        }

        String key = buildKey(identifier);
        redisTemplate.delete(key);
        log.info("Rate limit reset for {}", identifier);
    }

    /**
     * Build Redis key with current minute timestamp
     */
    private String buildKey(String identifier) {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        return keyPrefix + identifier + ":" + now;
    }
}
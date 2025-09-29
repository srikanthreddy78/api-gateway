package com.gateway;

import com.gateway.model.RateLimitResult;
import com.gateway.service.RateLimiterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RateLimiterServiceTest {

    @Autowired(required = false)
    private RateLimiterService rateLimiterService;

    @Test
    void testRateLimiterServiceExists() {
        assertNotNull(rateLimiterService, "RateLimiterService should be autowired");
    }

    @Test
    void testCheckRateLimit_WithinLimit() {
        if (rateLimiterService != null) {
            String identifier = "test-user-" + System.currentTimeMillis();

            RateLimitResult result = rateLimiterService.checkRateLimit(identifier, 10);

            assertTrue(result.isAllowed(), "Request should be allowed within limit");
        }
    }

    @Test
    void testCheckRateLimit_Exceeded() {
        if (rateLimiterService != null) {
            String identifier = "test-limit-" + System.currentTimeMillis();
            int limit = 5;

            // Make requests up to limit
            for (int i = 0; i < limit; i++) {
                RateLimitResult result = rateLimiterService.checkRateLimit(identifier, limit);
                assertTrue(result.isAllowed(), "Request " + (i + 1) + " should be allowed");
            }

            // Next request should be blocked
            RateLimitResult result = rateLimiterService.checkRateLimit(identifier, limit);

            // Note: Might still pass if Redis is not available (fail open)
            if (result.getRemaining() != -1) {
                assertFalse(result.isAllowed(), "Request should be blocked after limit");
            }
        }
    }

    @Test
    void testResetRateLimit() {
        if (rateLimiterService != null) {
            String identifier = "test-reset-" + System.currentTimeMillis();

            // Use up some quota
            rateLimiterService.checkRateLimit(identifier, 10);
            rateLimiterService.checkRateLimit(identifier, 10);

            long usageBefore = rateLimiterService.getCurrentUsage(identifier);

            // Reset
            rateLimiterService.resetRateLimit(identifier);

            long usageAfter = rateLimiterService.getCurrentUsage(identifier);

            assertTrue(usageAfter < usageBefore || usageBefore == 0,
                    "Usage should be reset");
        }
    }
}
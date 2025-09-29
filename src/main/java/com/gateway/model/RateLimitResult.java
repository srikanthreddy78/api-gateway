package com.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitResult {

    private boolean allowed;
    private long remaining;
    private long resetAt;
    private String reason;

    public static RateLimitResult allowed(long remaining) {
        return RateLimitResult.builder()
                .allowed(true)
                .remaining(remaining)
                .build();
    }

    public static RateLimitResult exceeded(String reason) {
        return RateLimitResult.builder()
                .allowed(false)
                .remaining(0)
                .reason(reason)
                .build();
    }

    public static RateLimitResult failOpen() {
        return RateLimitResult.builder()
                .allowed(true)
                .remaining(-1)
                .reason("Redis unavailable - failing open")
                .build();
    }
}
package com.gateway.model;

import lombok.Getter;

@Getter
public enum UserTier {
    FREE(100),      // 100 requests per hour
    BASIC(1000),    // 1000 requests per hour
    PRO(10000),     // 10,000 requests per hour
    ENTERPRISE(100000); // 100,000 requests per hour

    private final int requestsPerHour;

    UserTier(int requestsPerHour) {
        this.requestsPerHour = requestsPerHour;
    }
}
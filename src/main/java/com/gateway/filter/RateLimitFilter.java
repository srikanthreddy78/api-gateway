package com.gateway.filter;

import com.gateway.model.RateLimitResult;
import com.gateway.service.RateLimiterService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@Order(2)
public class RateLimitFilter implements Filter {

    @Autowired
    private RateLimiterService rateLimiterService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Skip rate limiting for actuator endpoints
        if (httpRequest.getRequestURI().startsWith("/actuator")) {
            chain.doFilter(request, response);
            return;
        }

        // Get identifier (API key or IP address)
        String identifier = getIdentifier(httpRequest);

        // Check rate limit
        RateLimitResult result = rateLimiterService.checkRateLimit(identifier);

        // Add rate limit headers
        httpResponse.addHeader("X-RateLimit-Limit", "100");
        httpResponse.addHeader("X-RateLimit-Remaining", String.valueOf(result.getRemaining()));

        if (!result.isAllowed()) {
            log.warn("Rate limit exceeded for: {}", identifier);
            httpResponse.setStatus(429);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write(
                    "{\"error\": \"Rate limit exceeded\", " +
                            "\"message\": \"" + result.getReason() + "\", " +
                            "\"limit\": 100, " +
                            "\"window\": \"1 minute\"}"
            );
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * Get identifier from API key or IP address
     */
    private String getIdentifier(HttpServletRequest request) {
        // Try API key first
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null && !apiKey.isEmpty()) {
            return "apikey:" + apiKey;
        }

        // Fall back to IP address
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return "ip:" + ip;
    }
}
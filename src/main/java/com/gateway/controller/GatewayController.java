package com.gateway.controller;

import com.gateway.service.ProxyService;
import com.gateway.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class GatewayController {

    @Autowired
    private ProxyService proxyService;

    @Autowired
    private RateLimiterService rateLimiterService;

    /**
     * Main gateway endpoint - forwards all API requests
     */
    @RequestMapping(value = "/api/**", method = {
            RequestMethod.GET,
            RequestMethod.POST,
            RequestMethod.PUT,
            RequestMethod.DELETE,
            RequestMethod.PATCH
    })
    public ResponseEntity<String> gateway(HttpServletRequest request) {

        // Read request body
        String body = readBody(request);

        // Forward to backend service
        return proxyService.forwardRequest(request, body);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("application", "API Gateway");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    /**
     * Admin endpoint - Reset rate limit
     */
    @PostMapping("/admin/rate-limit/reset")
    public ResponseEntity<Map<String, String>> resetRateLimit(@RequestParam String identifier) {
        rateLimiterService.resetRateLimit(identifier);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Rate limit reset successfully");
        response.put("identifier", identifier);

        return ResponseEntity.ok(response);
    }

    /**
     * Admin endpoint - Check rate limit status
     */
    @GetMapping("/admin/rate-limit/status")
    public ResponseEntity<Map<String, Object>> getRateLimitStatus(@RequestParam String identifier) {
        long usage = rateLimiterService.getCurrentUsage(identifier);

        Map<String, Object> response = new HashMap<>();
        response.put("identifier", identifier);
        response.put("current_usage", usage);
        response.put("limit", 100);
        response.put("remaining", Math.max(0, 100 - usage));

        return ResponseEntity.ok(response);
    }

    /**
     * Read request body
     */
    private String readBody(HttpServletRequest request) {
        try {
            BufferedReader reader = request.getReader();
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
            return body.toString();
        } catch (IOException e) {
            log.error("Error reading request body: {}", e.getMessage());
            return "";
        }
    }
}
package com.gateway.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@Order(1)
public class LoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Generate request ID
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        httpRequest.setAttribute("requestId", requestId);
        httpResponse.addHeader("X-Request-ID", requestId);

        // Log request
        long startTime = Instant.now().toEpochMilli();
        log.info("[{}] --> {} {} from {}",
                requestId,
                httpRequest.getMethod(),
                httpRequest.getRequestURI(),
                httpRequest.getRemoteAddr());

        try {
            chain.doFilter(request, response);
        } finally {
            // Log response
            long duration = Instant.now().toEpochMilli() - startTime;
            log.info("[{}] <-- {} in {}ms",
                    requestId,
                    httpResponse.getStatus(),
                    duration);
        }
    }
}
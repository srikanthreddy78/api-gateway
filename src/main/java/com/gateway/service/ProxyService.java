package com.gateway.service;

import com.gateway.model.RouteDefinition;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Enumeration;

@Slf4j
@Service
public class ProxyService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RouteDefinition routeDefinition;

    /**
     * Forward request to appropriate backend service
     */
    public ResponseEntity<String> forwardRequest(HttpServletRequest request, String body) {

        try {
            // Find matching route
            RouteDefinition.Route route = findRoute(request.getRequestURI());

            if (route == null) {
                log.warn("No route found for path: {}", request.getRequestURI());
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("{\"error\": \"No route found for: " + request.getRequestURI() + "\"}");
            }

            // Build target URL
            String targetUrl = buildTargetUrl(route, request);

            // Build headers
            HttpHeaders headers = buildHeaders(request);

            // Create request entity
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            // Log forwarding
            log.info("Forwarding: {} {} -> {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    targetUrl);

            // Forward request
            ResponseEntity<String> response = restTemplate.exchange(
                    targetUrl,
                    HttpMethod.valueOf(request.getMethod()),
                    entity,
                    String.class
            );

            log.info("Response: {} from {}", response.getStatusCode(), targetUrl);

            return response;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error: {} - {}", e.getStatusCode(), e.getMessage());
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());

        } catch (Exception e) {
            log.error("Error forwarding request: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Find matching route for request path
     */
    private RouteDefinition.Route findRoute(String path) {
        return routeDefinition.getRoutes().stream()
                .filter(route -> route.matches(path))
                .findFirst()
                .orElse(null);
    }

    /**
     * Build target URL from route and request
     */
    private String buildTargetUrl(RouteDefinition.Route route, HttpServletRequest request) {
        String path = request.getRequestURI();

        // Strip prefix if configured
        if (route.getStripPrefix() > 0) {
            String[] parts = path.split("/");
            if (parts.length > route.getStripPrefix()) {
                StringBuilder newPath = new StringBuilder();
                for (int i = route.getStripPrefix() + 1; i < parts.length; i++) {
                    newPath.append("/").append(parts[i]);
                }
                path = newPath.toString();
                if (path.isEmpty()) {
                    path = "/";
                }
            }
        }

        // Add query string if present
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isEmpty()) {
            path += "?" + queryString;
        }

        return route.getUri() + path;
    }

    /**
     * Build headers from original request
     */
    private HttpHeaders buildHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();

        // Copy headers from original request
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();

                // Skip hop-by-hop headers
                if (isHopByHopHeader(headerName)) {
                    continue;
                }

                Enumeration<String> headerValues = request.getHeaders(headerName);
                while (headerValues.hasMoreElements()) {
                    headers.add(headerName, headerValues.nextElement());
                }
            }
        }

        // Add forwarding headers
        headers.add("X-Forwarded-For", request.getRemoteAddr());
        headers.add("X-Forwarded-Proto", request.getScheme());
        headers.add("X-Forwarded-Host", request.getServerName());

        return headers;
    }

    /**
     * Check if header is hop-by-hop (should not be forwarded)
     */
    private boolean isHopByHopHeader(String headerName) {
        String lower = headerName.toLowerCase();
        return lower.equals("connection") ||
                lower.equals("keep-alive") ||
                lower.equals("proxy-authenticate") ||
                lower.equals("proxy-authorization") ||
                lower.equals("te") ||
                lower.equals("trailers") ||
                lower.equals("transfer-encoding") ||
                lower.equals("upgrade");
    }
}
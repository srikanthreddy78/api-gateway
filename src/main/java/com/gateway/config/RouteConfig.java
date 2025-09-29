package com.gateway.config;

import com.gateway.model.RouteDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Slf4j
@Configuration
public class RouteConfig {

    @Autowired
    private RouteDefinition routeDefinition;

    @PostConstruct
    public void logRoutes() {
        log.info("========================================");
        log.info("Configured Routes:");
        routeDefinition.getRoutes().forEach(route ->
                log.info("  {} -> {} ({})", route.getPath(), route.getUri(), route.getId())
        );
        log.info("========================================");
    }
}
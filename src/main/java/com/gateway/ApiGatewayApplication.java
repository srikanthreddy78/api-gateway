package com.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("✅ API Gateway Started Successfully!");
        System.out.println("========================================");
        System.out.println("Gateway URL:    http://localhost:8080");
        System.out.println("Health Check:   http://localhost:8080/actuator/health");
        System.out.println("Metrics:        http://localhost:8080/actuator/metrics");
        System.out.println("Prometheus:     http://localhost:8080/actuator/prometheus");
        System.out.println("========================================\n");
    }
}
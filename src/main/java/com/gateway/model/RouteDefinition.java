package com.gateway.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "gateway")
public class RouteDefinition {

    private List<Route> routes = new ArrayList<>();

    @Data
    public static class Route {
        private String id;
        private String path;
        private String uri;
        private int stripPrefix = 0;

        public boolean matches(String requestPath) {
            String pattern = path.replace("/**", "");
            return requestPath.startsWith(pattern);
        }
    }
}
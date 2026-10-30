package com.pulsefit.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/api/auth", "/api/auth/**")
                        .filters(f -> f.stripPrefix(1)).uri("lb://auth-service"))
                .route("member-service", r -> r.path("/api/members", "/api/members/**")
                        .filters(f -> f.stripPrefix(1)).uri("lb://member-service"))
                .route("subscription-service", r -> r.path("/api/subscriptions", "/api/subscriptions/**")
                        .filters(f -> f.stripPrefix(1)).uri("lb://subscription-service"))
                .route("attendance-service", r -> r.path("/api/attendance", "/api/attendance/**")
                        .filters(f -> f.stripPrefix(1)).uri("lb://attendance-service"))
                .route("plan-service", r -> r.path("/api/plans", "/api/plans/**")
                        .filters(f -> f.stripPrefix(1)).uri("lb://subscription-service"))
                .build();
    }
}

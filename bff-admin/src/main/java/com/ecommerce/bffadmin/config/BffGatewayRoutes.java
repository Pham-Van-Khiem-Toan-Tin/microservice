package com.ecommerce.bffadmin.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class BffGatewayRoutes {
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("identity-service", r -> r.path("/auth/**")
                        .filters(f -> f.stripPrefix(1).tokenRelay())
                        .uri("lb://identity-service"))
                .route("catalog-service", r -> r.path("/api/admin/catalog/**")
                        .filters(f -> f.stripPrefix(3).tokenRelay())
                        .uri("lb://catalog-service"))
                .route("catalog-service", r -> r.path("/api/admin/orders/**", "/api/admin/stats/**")
                        .filters(f -> f.stripPrefix(2).tokenRelay())
                        .uri("lb://order-service"))
                .route("inventory-service", r -> r.path("/api/admin/inventories/**", "/api/admin/inventory/**")
                        .filters(f -> f.stripPrefix(2).tokenRelay())
                        .uri("lb://inventory-service"))
                .build();
    }
}


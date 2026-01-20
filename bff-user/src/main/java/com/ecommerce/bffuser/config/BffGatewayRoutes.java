package com.ecommerce.bffuser.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class BffGatewayRoutes {
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("identity-service", r -> r.path("/auth/**", "/api/wishlist/**",
                                "/api/locations/**", "/api/user-addresses/**")
                        .filters(f -> f.stripPrefix(1).tokenRelay())
                        .uri("lb://identity-service"))
                .route("catalog-service", r -> r.path("/api/catalog/products/**", "/api/catalog/reviews/**")
                        .filters(f -> f.stripPrefix(2).tokenRelay())
                        .uri("lb://catalog-service"))
                .route("catalog-service", r -> r.path("/api/public/catalog/**")
                        .filters(f -> f.stripPrefix(3))
                        .uri("lb://catalog-service"))
                .route("search-service", r -> r.path("/api/search/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("lb://search-service"))
                .route("order-service", r -> r.path("/api/orders/**")
                        .filters(f -> f.stripPrefix(2).tokenRelay())
                        .uri("lb://order-service"))
                .route("payment-service", r -> r.path("/api/payments/**")
                        .filters(f -> f.stripPrefix(2).tokenRelay())
                        .uri("lb://payment-service"))
                .build();
    }
}


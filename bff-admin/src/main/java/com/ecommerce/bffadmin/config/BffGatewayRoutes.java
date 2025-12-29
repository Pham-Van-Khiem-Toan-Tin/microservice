package com.ecommerce.bffadmin.config;

import com.ecommerce.bffadmin.service.InternalTokenService;
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
                .build();
    }
}


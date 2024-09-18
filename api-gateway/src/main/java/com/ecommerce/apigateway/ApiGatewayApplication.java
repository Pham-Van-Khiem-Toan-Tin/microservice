package com.ecommerce.apigateway;

import com.netflix.discovery.EurekaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;

@SpringBootApplication
public class ApiGatewayApplication {
    @Autowired
    @Lazy
    private EurekaClient eurekaClient;
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("identity-service", r -> r.path("/login", "/register", "/auth/**", "/auth/gentoken")
                        .and().method(HttpMethod.GET, HttpMethod.POST)
                        .uri("lb://auth-service"))
                .route("order-service", r -> r.path("/order/**").uri("lb://order-service"))
                .route("auth-service", r -> r.path("/token/**").uri("lb://auth-service"))
                .build();
    }
}

package com.ecommerce.apigateway;

import com.netflix.discovery.EurekaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

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
                .route("identity-service", r -> r.path("/auth/**", "/account/**").uri("lb://identity-service"))
                .route("payment-service", r -> r.path("/payment/**").uri("lb://payment-service"))
                .route("category-service", r -> r.path("/category/**").uri("lb://category-service"))
                .route("order-service", r -> r.path("/order/**").uri("lb://order-service"))
                .build();
    }

}

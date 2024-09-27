package com.ecommerce.apigateway.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
public class GatewayConfig {
//    @Bean
//    public RestTemplate restTemplate(RestTemplateBuilder builder) {
//        return builder.build();
//    }
//
//    public String extractToken(ServerHttpRequest request) {
//        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            return authHeader.substring(7);
//        }
//        return null;
//    }
//    public boolean isValidateToken(String token) {
//        RestTemplate restTemplate = new RestTemplate();
//        String url = "http://localhost:8085/verify-token";
//        Map<String, Object> requestData = new HashMap<>();
//        requestData.put("token", token);
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Content-Type", "application/json");
//
//        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestData, headers);
//        ResponseEntity<String> response = restTemplate.postForEntity(url,request, String.class );
//        System.out.println("Response body: " + response.getBody());
//
//    }
}

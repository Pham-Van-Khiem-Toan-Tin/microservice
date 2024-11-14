package com.ecommerce.apigateway.config;

import com.ecommerce.apigateway.service.IntrospectService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    IntrospectService introspectService;
    @NonFinal
    private String[]  publicEndpoints = {
            "/identity/auth/introspect",
            "/identity/register",
            "/identity/login",
            "/identity/user/test"
    };
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Authentication filter");
        if (isPublicEndpoint(exchange.getRequest()))
            return chain.filter(exchange);
        List<String> authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (CollectionUtils.isEmpty(authHeader))
            return unauthenticate(exchange.getResponse());
        String token = authHeader.get(0).replace("Bearer ", "");
        log.info("Token: {}", token);

        return introspectService.introspect(token).flatMap(rs -> {
            if (rs != null && rs.getUsername() != null) {
                return chain.filter(exchange);
            } else {
                return unauthenticate(exchange.getResponse());
            }
        }).onErrorResume(throwable -> unauthenticate(exchange.getResponse()));
    }

    @Override
    public int getOrder() {
        return -1;
    }
    Mono<Void> unauthenticate(ServerHttpResponse response) {
        Map<String, Object> apiResponse = new HashMap<>();
        apiResponse.put("message", "Unauthenticated");
        String body = null;
        try {
            body = objectMapper.writeValueAsString(apiResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }
    private boolean isPublicEndpoint(ServerHttpRequest request) {
        return Arrays.stream(publicEndpoints)
                .anyMatch(s -> request.getURI().getPath().matches(s));
    }
}

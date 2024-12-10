package com.ecommerce.apigateway.config;

import com.ecommerce.apigateway.dto.IntrospectResponse;
import com.ecommerce.apigateway.service.IntrospectService;
import com.ecommerce.apigateway.utils.JwtUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    @Value("${jwt.privateKey}")
    private String privateKey;
    @NonFinal
    private String[] publicEndpoints = {
            "/auth/introspect",
            "/auth/token",
            "/auth/login",
            "/auth/register",
            "/oauth2/token"
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
        String sessionId = exchange.getRequest().getCookies().get("session_id").get(0).getValue();
        if (!StringUtils.hasText(token) || !StringUtils.hasText(sessionId))
            return unauthenticate(exchange.getResponse());
        log.info("Token: {}", token);
        return introspectService.introspect(token, sessionId).flatMap(rs -> {
            if (rs != null) {
                String tokenInternal = generateTokenInternal(rs);
                exchange.getRequest()
                        .mutate()
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenInternal);
                return chain.filter(exchange);
            } else {
                return unauthenticate(exchange.getResponse());
            }
        }).onErrorResume(throwable -> {
            HttpStatusCode status;
            String errorMessage;
            if (throwable instanceof WebClientResponseException ex) {
                status = ex.getStatusCode();
                errorMessage = ex.getResponseBodyAsString();
            } else {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                errorMessage = "Không thể xửu lí yêu cầu vui lòng thử lại sau";
            }
            exchange.getResponse().setStatusCode(status);
            exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
            return exchange.getResponse()
                    .writeWith(Mono.just(exchange
                            .getResponse()
                            .bufferFactory()
                            .wrap(errorMessage.getBytes(StandardCharsets.UTF_8))));
        });
    }
    private String generateTokenInternal(IntrospectResponse user) {
        SecretKey secretKey = JwtUtils.getSecretKey(privateKey);
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole());
        claims.put("functions", user.getFunctions());
        claims.put("subfunctions", user.getSubfunctions());
        LocalDateTime currentTime = LocalDateTime.now();
        long expiration = currentTime.plusMinutes(5).atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant().toEpochMilli();
        return JwtUtils.generateToken(user.getEmail(), claims, expiration, secretKey);
    }
    @Override
    public int getOrder() {
        return -1;
    }

    Mono<Void> unauthenticate(ServerHttpResponse response) {
        Map<String, Object> apiResponse = new HashMap<>();
        apiResponse.put("message", "Vui lòng đăng nhập để truy cập tài nguyên");
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

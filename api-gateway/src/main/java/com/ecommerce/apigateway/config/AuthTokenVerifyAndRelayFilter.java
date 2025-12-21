package com.ecommerce.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;

import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Component;

import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.List;

@Slf4j
@Component
public class AuthTokenVerifyAndRelayFilter implements GlobalFilter, Ordered {

    private final ReactiveSessionRepository<? extends Session> sessionRepository;
    private final ReactiveJwtDecoder jwtDecoder;

    // 1. Khai báo công cụ so khớp đường dẫn của Spring
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // 2. Định nghĩa danh sách API Public (Có thể đưa vào application.yml để config động)
    private final List<String> publicEndpoints = List.of(
            "/auth/oauth2/**",           // Các API login/register
            "/auth/login/oauth2/**",         // Các API login oauth2
            "/public/**",         // Các API công khai khác
            "/api/v1/products/**" // Ví dụ: Xem sản phẩm không cần login
    );

    public AuthTokenVerifyAndRelayFilter(ReactiveSessionRepository<? extends Session> sessionRepository,
                                         ReactiveJwtDecoder jwtDecoder) {
        this.sessionRepository = sessionRepository;
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestPath = exchange.getRequest().getPath().value();

        // 3. CHECK PUBLIC API: Nếu path nằm trong whitelist -> Cho qua ngay lập tức
        if (isPublicEndpoint(requestPath)) {
            return chain.filter(exchange);
        }

        // --- Logic xác thực bên dưới giữ nguyên ---

        HttpCookie sessionCookie = exchange.getRequest().getCookies().getFirst("BFF_SESSION");
        if (sessionCookie == null) {
            // Nếu truy cập API bảo mật mà không có cookie -> Trả về 401 ngay
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String sessionId = new String(Base64.getDecoder().decode(sessionCookie.getValue()));
        return sessionRepository.findById(sessionId)
                .flatMap(session -> {
                    String accessToken = session.getAttribute("BFF_ACCESS_TOKEN");

                    if (accessToken == null) {
                        return Mono.error(new Exception("Token not found"));
                    }
                    String rolesJson = session.getAttribute("AUTHORITIES");

                    return jwtDecoder.decode(accessToken)
                            .flatMap(jwt -> {
                                ServerWebExchange mutatedExchange = exchange.mutate()
                                        .request(r -> {
                                            r.header("Authorization", "Bearer " + accessToken);
                                            if (rolesJson != null) {
                                                r.header("X-Auth-Roles", rolesJson);
                                            }
                                        })
                                        .build();
                                return chain.filter(mutatedExchange);
                            });
                })
                .onErrorResume(e -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("No session found for sessionId {}", sessionId);
                    // Trường hợp tìm session trong Redis ra null (hết hạn)
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }));
    }

    // Hàm hỗ trợ kiểm tra path
    private boolean isPublicEndpoint(String requestPath) {
        for (String pattern : publicEndpoints) {
            if (pathMatcher.match(pattern, requestPath)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return -1;
    }
}

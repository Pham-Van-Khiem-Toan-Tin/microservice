package com.ecommerce.authservice.config;

import com.ecommerce.authservice.service.InternalTokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.stream.Collectors;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.stripPrefix;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.before;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;

@Configuration
public class BffGatewayRoutes {
    @Bean
    public RouterFunction<ServerResponse> catalogProxy(InternalTokenService tokenService) {

        // 1. Định nghĩa Filter sửa đổi Request (Auth Logic)
        HandlerFilterFunction<ServerResponse, ServerResponse> authFilter = (request, next) -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            boolean realUser = auth != null
                    && auth.isAuthenticated()
                    && !(auth instanceof AnonymousAuthenticationToken);

            // Case 1: Khách vãng lai (Chưa login) -> Xóa Authorization header cũ (nếu có) để tránh lọt
            if (!realUser) {
                ServerRequest cleanRequest = ServerRequest.from(request)
                        .headers(headers -> headers.remove(HttpHeaders.AUTHORIZATION))
                        .build();
                return next.handle(cleanRequest);
            }

            // Case 2: User thật -> Gen Token nội bộ và thay thế Header
            String jwt = tokenService.mint(auth);

            // Lấy Authorities
            String authorities = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .distinct()
                    .collect(Collectors.joining(","));

            // Clone request cũ, thêm header mới
            ServerRequest modifiedRequest = ServerRequest.from(request)
                    .headers(headers -> {
                        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
                        headers.set("X-Authorities", authorities);
                    })
                    .build();

            // QUAN TRỌNG: Chuyển request đã sửa cho chain tiếp theo
            return next.handle(modifiedRequest);
        };

        // 2. Định nghĩa Route
        return route("catalog_proxy")
                .route(path("/api/catalog/**"), http()) 
                .before(uri("http://localhost:8083"))// URI đích đặt ở đây
                .filter(stripPrefix(2)) // /api/catalog/products -> /products
                .filter(authFilter)     // Áp dụng filter auth đã viết ở trên
                .build();
    }
}


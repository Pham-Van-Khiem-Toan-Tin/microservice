package com.ecommerce.authservice.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CustomRoleHeaderFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String rolesJson = request.getHeader("X-Auth-Roles");

        if (rolesJson != null && SecurityContextHolder.getContext().getAuthentication() != null) {
            try {
                // 1. Parse JSON ra List String
                List<String> roles = new ObjectMapper().readValue(rolesJson, new TypeReference<List<String>>(){});

                // 2. Convert thành GrantedAuthority
                Set<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet());

                // 3. Update lại Security Context hiện tại
                Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();

                // Tạo Authentication mới bao gồm cả thông tin User cũ + Role mới từ Header
                UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                        currentAuth.getPrincipal(),
                        currentAuth.getCredentials(),
                        authorities // <--- QUAN TRỌNG: Role lấy từ DB BFF
                );

                SecurityContextHolder.getContext().setAuthentication(newAuth);

            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}

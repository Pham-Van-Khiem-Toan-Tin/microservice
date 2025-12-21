package com.ecommerce.authservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SessionDebugFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();

        // 1. Bắt log lúc bắt đầu đi (Redirect sang IDP)
        if (uri.startsWith("/oauth2/authorization")) {
            HttpSession session = request.getSession(false);
            String sessionId = (session != null) ? session.getId() : "null (Chưa có session)";
            System.out.println("=================================================");
            System.out.println("DEBUG: [LÚC ĐI] URI: " + uri);
            System.out.println("DEBUG: [LÚC ĐI] Session ID: " + sessionId);
            System.out.println("=================================================");
        }

        // 2. Bắt log lúc quay về (Callback từ IDP)
        if (uri.startsWith("/login/oauth2/code")) {
            HttpSession session = request.getSession(false);
            String sessionId = (session != null) ? session.getId() : "null (MẤT SESSION!)";
            System.out.println("=================================================");
            System.out.println("DEBUG: [LÚC VỀ] URI: " + uri);
            System.out.println("DEBUG: [LÚC VỀ] Session ID: " + sessionId);
            // In thêm Cookie header để xem trình duyệt có gửi cookie không
            String cookieHeader = request.getHeader("Cookie");
            System.out.println("DEBUG: [LÚC VỀ] Cookie Header: " + cookieHeader);
            System.out.println("=================================================");
        }

        // Cho phép request đi tiếp
        filterChain.doFilter(request, response);
    }


}

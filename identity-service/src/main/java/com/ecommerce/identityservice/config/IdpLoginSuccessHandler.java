package com.ecommerce.identityservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class IdpLoginSuccessHandler implements AuthenticationSuccessHandler {
    private static final String ADMIN_INIT_URL = "http://localhost:8082/auth/oauth2/authorization/admin-idp";
    private static final String CUSTOMER_INIT_URL = "http://localhost:8082/auth/oauth2/authorization/user-idp";

    // Mặc định Spring dùng cái này để lưu request trước khi bị đá sang trang login
    private final RequestCache requestCache = new HttpSessionRequestCache();
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            // Nếu có, cho đi tiếp theo đúng quy trình cũ (Flow chuẩn)
            redirectStrategy.sendRedirect(request, response, savedRequest.getRedirectUrl());
            return;
        }
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        if (roles.contains("SUPER_ADMIN") || roles.contains("ADMIN") || roles.contains("EMPLOYEE")) {
            // Nếu là Admin -> Đẩy sang luồng khởi tạo Admin của BFF
            // BFF sẽ tạo State -> Redirect lại IdP (Silent) -> Về trang Admin Dashboard
            redirectStrategy.sendRedirect(request, response, ADMIN_INIT_URL);
        }
        else if (roles.contains("CUSTOMER")) {
            // Tương tự với Customer
            redirectStrategy.sendRedirect(request, response, CUSTOMER_INIT_URL);
        }
        else {
            // Trường hợp user không có quyền gì (hoặc role lạ)
            // Đưa về trang thông tin tài khoản hoặc trang lỗi của IDP
            redirectStrategy.sendRedirect(request, response, "/terms");
        }
    }
}

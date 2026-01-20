package com.ecommerce.catalogservice.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignClientConfig {
    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            // 1. Lấy thông tin request hiện tại từ Context của Spring
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                // 2. Lấy header Authorization (chứa Bearer token) từ request gốc
                String authHeader = request.getHeader("Authorization");

                // 3. Nếu có token, gán nó vào request của Feign
                if (authHeader != null) {
                    template.header("Authorization", authHeader);
                }
            }
        };
    }
}

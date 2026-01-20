package com.ecommerce.catalogservice.integration;

import com.ecommerce.catalogservice.dto.response.user.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "identity-service",
        url = "${application.config.identity-url}")
public interface IdentityFeignClient {
    @GetMapping("/profile/review")
    UserResponse getUserProfile();
}

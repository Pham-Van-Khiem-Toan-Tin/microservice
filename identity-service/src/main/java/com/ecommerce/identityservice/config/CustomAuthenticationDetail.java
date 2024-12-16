package com.ecommerce.identityservice.config;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomAuthenticationDetail {
    private String clientId;
    private String token;
}

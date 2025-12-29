package com.ecommerce.identityservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class AuthDTO {
    private String userId;
    private String userName;
    private Set<String> permissions;
}

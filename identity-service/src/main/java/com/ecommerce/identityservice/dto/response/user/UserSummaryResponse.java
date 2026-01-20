package com.ecommerce.identityservice.dto.response.user;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserSummaryResponse {
    private String id;
    private String fullName;
    private String email;
    private String phone;
    private String avatarUrl;
    private String roleName;
    private int status;
    private Instant createdAt;
}

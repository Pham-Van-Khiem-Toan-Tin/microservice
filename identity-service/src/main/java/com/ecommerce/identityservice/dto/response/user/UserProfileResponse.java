package com.ecommerce.identityservice.dto.response.user;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserProfileResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String avatarUrl;
    private Boolean verifyEmail; // Để hiển thị trạng thái xác thực
    private Instant createdAt;
}

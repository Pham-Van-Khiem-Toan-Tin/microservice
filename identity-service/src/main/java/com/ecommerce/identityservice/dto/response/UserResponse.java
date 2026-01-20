package com.ecommerce.identityservice.dto.response;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String avatarUrl;

    // Helper để lấy tên đầy đủ
}

package com.ecommerce.catalogservice.dto.response.user;


import lombok.Data;

@Data
public class UserResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String avatarUrl;

    // Helper để lấy tên đầy đủ
    public String getFullName() {
        return firstName + " " + lastName;
    }
}

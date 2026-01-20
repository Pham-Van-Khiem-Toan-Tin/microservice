package com.ecommerce.identityservice.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class UserProfileDto {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String avatarUrl;
    private String avatarPublicId;
    private LocalDate joinDate; // Ngày tham gia

    // Info từ Payment Service
    private BigDecimal walletBalance;
}

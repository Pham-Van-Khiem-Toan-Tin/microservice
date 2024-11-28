package com.ecommerce.identityservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileDetailDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String avatar;
    private String role;
    private BillingDTO billing;
}

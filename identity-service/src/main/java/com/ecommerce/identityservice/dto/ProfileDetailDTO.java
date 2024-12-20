package com.ecommerce.identityservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileDetailDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String avatar;
    private String phoneNumber;
    private BillingDTO billing;
}

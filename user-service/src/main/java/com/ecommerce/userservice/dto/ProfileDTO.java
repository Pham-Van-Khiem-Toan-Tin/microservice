package com.ecommerce.userservice.dto;

import lombok.Data;

@Data
public class ProfileDTO {
    private String firstName;
    private String lastName;
    private String avatar;
    private String role;
    private BillingDTO billing;
}

package com.ecommerce.userservice.dto;

import lombok.Data;

@Data
public class BillingDTO {
    private String email;
    private String address;
    private String firstName;
    private String lastName;
    private String phoneNumber;
}

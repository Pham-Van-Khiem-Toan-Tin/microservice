package com.ecommerce.identityservice.dto;

import lombok.Data;

@Data
public class BillingDTO {
    private String firstName;
    private String lastName;
    private String address;
    private String companyName;
    private String email;
    private String phoneNumber;
    private String country;
    private String states;
    private String zipCode;
}

package com.ecommerce.payentservice.dto;

import lombok.Data;

@Data
public class BillingDTO {
    private String firstName;
    private String lastName;
    private String address;
    private String email;
    private String phoneNumber;
}

package com.ecommerce.payentservice.form;

import lombok.Data;

@Data
public class UpdateBillingForm {
    private String firstName;
    private String lastName;
    private String address;
    private String company;
    private String email;
    private String phoneNumber;
    private String country;
    private Long states;
    private String zipCode;
}

package com.ecommerce.identityservice.form;

import lombok.Data;

@Data
public class UpdateProfileForm {
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private BillingForm billing;
}

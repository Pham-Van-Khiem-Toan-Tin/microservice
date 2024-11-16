package com.ecommerce.identityservice.form;

import lombok.Data;

@Data
public class RegisterForm {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
}

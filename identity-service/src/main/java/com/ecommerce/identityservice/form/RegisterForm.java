package com.ecommerce.identityservice.form;

import lombok.Data;

@Data
public class RegisterForm {
    private String userName;
    private String email;
    private String password;
}

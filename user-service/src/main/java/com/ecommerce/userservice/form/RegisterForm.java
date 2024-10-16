package com.ecommerce.userservice.form;

import lombok.Data;

@Data
public class RegisterForm {
    private String name;
    private String email;
    private String password;
}

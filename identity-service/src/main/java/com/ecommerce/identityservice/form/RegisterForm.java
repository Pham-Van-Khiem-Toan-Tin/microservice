package com.ecommerce.identityservice.form;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class RegisterForm {
    private String name;
    private String email;
    private String password;
}

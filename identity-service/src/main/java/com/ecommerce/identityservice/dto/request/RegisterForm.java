package com.ecommerce.identityservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterForm {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String confirmPassword;
}

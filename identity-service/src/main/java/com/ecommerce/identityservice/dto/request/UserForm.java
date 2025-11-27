package com.ecommerce.identityservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserForm {
    private String fullName;
    private String email;
    private String password;
    private String confirmPassword;
}

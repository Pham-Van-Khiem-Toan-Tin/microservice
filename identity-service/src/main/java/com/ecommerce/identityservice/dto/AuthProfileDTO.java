package com.ecommerce.identityservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class AuthProfileDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private Set<String> functions;
    private Set<String> subfunctions;
}

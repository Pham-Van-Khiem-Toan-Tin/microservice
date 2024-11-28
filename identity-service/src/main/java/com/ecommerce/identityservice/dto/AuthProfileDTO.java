package com.ecommerce.identityservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AuthProfileDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private List<String> functions;
    private List<String> subfunctions;
}

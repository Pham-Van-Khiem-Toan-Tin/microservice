package com.ecommerce.identityservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class IntrospectDTO {
    private String email;
    private String role;
    private List<String> functions;
    private List<String> subfunctions;
}

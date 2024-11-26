package com.ecommerce.apigateway.dto;

import lombok.Data;

import java.util.List;

@Data
public class IntrospectResponse {
    private String email;
    private String role;
    private List<String> functions;
    private List<String> subfunctions;
}

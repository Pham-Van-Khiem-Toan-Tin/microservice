package com.ecommerce.apigateway.dto;

import lombok.Data;

import java.util.List;

@Data
public class IntrospectResponse {
    private String username;
    private String email;
    private List<String> role;
}

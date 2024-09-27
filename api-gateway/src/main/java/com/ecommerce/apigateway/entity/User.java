package com.ecommerce.apigateway.entity;

import lombok.Data;

import java.util.List;

@Data
public class User {
    private String userId;
    private String userName;
    private List<String> roles;
}

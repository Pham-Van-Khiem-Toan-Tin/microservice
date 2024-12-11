package com.ecommerce.apigateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class IntrospectResponse {
    private Boolean active;
    private String sub;
    private List<String> aud;
    private Long nbf;
    private String scope;
    private String iss;
    private Long exp;
    private Long iat;
    private String jti;
    @JsonProperty("client_id")
    private String clientId;
    @JsonProperty("token_type")
    private String tokenType;
}

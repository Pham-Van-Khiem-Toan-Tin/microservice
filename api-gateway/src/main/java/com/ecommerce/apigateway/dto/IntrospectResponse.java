package com.ecommerce.apigateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class IntrospectResponse {
    private Boolean active;
    private String sub;
    private List<String> aud;
    private Long nbf;
    private String scope;
    private Map<String, Set<String>> roles;
    private String iss;
    private Long exp;
    private Long iat;
    private String jti;
    @JsonProperty("client_id")
    private String clientId;
    @JsonProperty("token_type")
    private String tokenType;
}

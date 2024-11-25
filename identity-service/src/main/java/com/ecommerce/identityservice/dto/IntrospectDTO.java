package com.ecommerce.identityservice.dto;

import io.jsonwebtoken.Claims;
import lombok.Data;

import java.util.List;

@Data
public class IntrospectDTO {
    private String email;
    private String role;
    private List<String> functions;
    private List<String> subfunctions;
    private String session;
    public static IntrospectDTO from(Claims claims) {
        IntrospectDTO introspectDTO = new IntrospectDTO();
        introspectDTO.setEmail(claims.getSubject());
        introspectDTO.setRole(claims.get("role", String.class));
        introspectDTO.setFunctions(claims.get("functions", List.class));
        introspectDTO.setSubfunctions(claims.get("subfunctions", List.class));
        return introspectDTO;
    }
}

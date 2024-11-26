package com.ecommerce.identityservice.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class LoginDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private List<String> functions;
    private List<String> subfunctions;
    private String accessToken;
    private long expireIn;
    private String refreshToken;
    private String sessionId;
    public static LoginDTO from(UserDTO user) {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail(user.getEmail());
        loginDTO.setFirstName(user.getFirstName());
        loginDTO.setLastName(user.getLastName());
        loginDTO.setRole(user.getRole());
        loginDTO.setFunctions(user.getFunctions());
        loginDTO.setSubfunctions(user.getSubfunctions());
        return loginDTO;
    }
}

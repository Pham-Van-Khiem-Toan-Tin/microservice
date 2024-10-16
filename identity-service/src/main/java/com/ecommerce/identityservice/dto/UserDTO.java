package com.ecommerce.identityservice.dto;

import com.ecommerce.identityservice.entity.User;
import lombok.Data;

@Data
public class UserDTO {
    private String id;
    private String name;
    private String email;
    private String role;
    public static UserDTO from(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setRole(user.getRole());
        return userDTO;
    }
}

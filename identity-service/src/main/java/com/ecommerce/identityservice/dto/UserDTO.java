package com.ecommerce.identityservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Data
public class UserDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private String role;
    private List<String> functions;
    private List<String> subfunctions;
    public static UserDTO from(Map<String, Object> userQuery) {
        UserDTO user = new UserDTO();
        user.setEmail((String) userQuery.get("email"));
        user.setFirstName((String) userQuery.get("first_name"));
        user.setLastName((String) userQuery.get("last_name"));
        user.setPassword((String) userQuery.get("password"));
        user.setRole((String) userQuery.get("role"));
        String functionString = (String)  userQuery.get("function_group");
        user.setFunctions(Arrays.stream(functionString.split(",")).toList());
        String subfunctionString = (String) userQuery.get("subfunction_group");
        user.setSubfunctions(Arrays.stream(subfunctionString.split(",")).toList());
        return user;
    }
}

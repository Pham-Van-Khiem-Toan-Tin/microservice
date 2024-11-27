package com.ecommerce.identityservice.dto;

import com.ecommerce.identityservice.utils.DateTimeUtils;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Data
public class ProfileDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private List<String> functions;
    private List<String> subfunctions;
    public static ProfileDTO toProfile(Map<String, Object> userQuery) {
        ProfileDTO user = new ProfileDTO();
        user.setEmail((String) userQuery.get("email"));
        user.setFirstName((String) userQuery.get("first_name"));
        user.setLastName((String) userQuery.get("last_name"));
        user.setRole((String) userQuery.get("role"));
        String functionString = (String)  userQuery.get("function_group");
        user.setFunctions(Arrays.stream(functionString.split(",")).toList());
        String subfunctionString = (String) userQuery.get("subfunction_group");
        user.setSubfunctions(Arrays.stream(subfunctionString.split(",")).toList());
        return user;
    }
    public static ProfileDTO toProfileBase(Map<String, Object> userQuery) {
        ProfileDTO user = new ProfileDTO();
        user.setEmail((String) userQuery.get("email"));
        user.setFirstName((String) userQuery.get("first_name"));
        user.setLastName((String) userQuery.get("last_name"));
        user.setRole((String) userQuery.get("role"));
        String functionString = (String)  userQuery.get("function_group");
        user.setFunctions(Arrays.stream(functionString.split(",")).toList());
        String subfunctionString = (String) userQuery.get("subfunction_group");
        user.setSubfunctions(Arrays.stream(subfunctionString.split(",")).toList());
        return user;
    }
}

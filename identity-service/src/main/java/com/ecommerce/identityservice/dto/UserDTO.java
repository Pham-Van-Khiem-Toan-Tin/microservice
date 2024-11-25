package com.ecommerce.identityservice.dto;

import com.ecommerce.identityservice.utils.DateTimeUtils;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;
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
    private Integer loginFailCount;
    private Boolean block;
    private LocalDateTime lockTime;
    private LocalDateTime unlockTime;
    private List<String> functions;
    private List<String> subfunctions;
    public static UserDTO from(Map<String, Object> userQuery) {
        UserDTO user = new UserDTO();
        user.setEmail((String) userQuery.get("email"));
        user.setFirstName((String) userQuery.get("first_name"));
        user.setLastName((String) userQuery.get("last_name"));
        user.setPassword((String) userQuery.get("password"));
        user.setRole((String) userQuery.get("role"));
        user.setLockTime(DateTimeUtils.convertTimeStampToLocalDateTime((Timestamp) userQuery.get("lock_time")));
        user.setUnlockTime(DateTimeUtils.convertTimeStampToLocalDateTime((Timestamp) userQuery.get("unlock_time")));
        user.setBlock((Boolean) userQuery.get("block"));
        user.setLoginFailCount((Integer) userQuery.get("login_fail_count"));
        String functionString = (String)  userQuery.get("function_group");
        user.setFunctions(Arrays.stream(functionString.split(",")).toList());
        String subfunctionString = (String) userQuery.get("subfunction_group");
        user.setSubfunctions(Arrays.stream(subfunctionString.split(",")).toList());
        return user;
    }
}

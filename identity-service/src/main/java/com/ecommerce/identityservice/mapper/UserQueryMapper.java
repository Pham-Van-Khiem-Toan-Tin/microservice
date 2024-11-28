package com.ecommerce.identityservice.mapper;

import com.ecommerce.identityservice.dto.AuthProfileDTO;
import com.ecommerce.identityservice.dto.UserDTO;
import com.ecommerce.identityservice.utils.DateTimeUtils;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Map;

public class UserQueryMapper {
    public static UserDTO toUserDTO(Map<String, Object> userQuery) {
        return UserDTO.builder()
                .email((String) userQuery.get("email"))
                .firstName((String) userQuery.get("first_name"))
                .lastName((String) userQuery.get("last_name"))
                .password((String) userQuery.get("password"))
                .role((String) userQuery.get("role"))
                .lockTime(DateTimeUtils.convertTimeStampToLocalDateTime((Timestamp) userQuery.get("lock_time")))
                .unlockTime(DateTimeUtils.convertTimeStampToLocalDateTime((Timestamp) userQuery.get("unlock_time")))
                .block((Boolean) userQuery.get("block"))
                .loginFailCount((Integer) userQuery.get("login_fail_count"))
                .functions(Arrays.stream(String.valueOf(userQuery.get("function_group")).split(",")).toList())
                .subfunctions(Arrays.stream(String.valueOf(userQuery.get("subfunction_group")).split(",")).toList())
                .build();
    }
    public static AuthProfileDTO toProfileBaseDTO(Map<String, Object> userQuery) {
        return AuthProfileDTO.builder()
                .email((String) userQuery.get("email"))
                .firstName((String) userQuery.get("first_name"))
                .lastName((String) userQuery.get("last_name"))
                .role((String) userQuery.get("role"))
                .functions(Arrays.stream(String.valueOf(userQuery.get("function_group")).split(",")).toList())
                .subfunctions(Arrays.stream(String.valueOf(userQuery.get("subfunction_group")).split(",")).toList())
                .build();
    }

}

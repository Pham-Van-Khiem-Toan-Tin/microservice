package com.ecommerce.identityservice.dto;

import com.ecommerce.identityservice.utils.DateTimeUtils;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}

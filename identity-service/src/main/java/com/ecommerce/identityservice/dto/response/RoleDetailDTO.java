package com.ecommerce.identityservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class RoleDetailDTO {
    private String id;
    private String code;
    private String name;
    private String description;
    private Set<String> subFunctions;
}

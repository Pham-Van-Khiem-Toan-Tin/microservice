package com.ecommerce.identityservice.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class RoleCreateForm {
    private String code;
    private String name;
    private String description;
    private Set<String> subFunctions;
}

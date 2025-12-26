package com.ecommerce.authservice.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class RoleCreateForm {
    private String id;
    private String name;
    private String description;
    private Set<String> subFunctions;
}

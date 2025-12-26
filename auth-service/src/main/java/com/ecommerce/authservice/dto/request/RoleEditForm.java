package com.ecommerce.authservice.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class RoleEditForm {
    private String oldId;
    private String newId;
    private String name;
    private String description;
    private Set<String> subFunctions;
}

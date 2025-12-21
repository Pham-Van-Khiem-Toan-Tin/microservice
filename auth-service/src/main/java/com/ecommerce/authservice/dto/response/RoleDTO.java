package com.ecommerce.authservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleDTO {
    private String id;
    private String name;
    private String description;
    private Long quantityPermission;

    public RoleDTO(String id, String name, String description, Long quantityPermission) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.quantityPermission = quantityPermission;
    }
}

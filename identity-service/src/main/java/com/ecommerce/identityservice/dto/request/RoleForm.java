package com.ecommerce.identityservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleForm {
    private String id;
    private String name;
    private String description;
    private int position;
}

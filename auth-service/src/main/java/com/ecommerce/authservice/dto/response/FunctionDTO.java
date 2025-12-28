package com.ecommerce.authservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FunctionDTO {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private Integer sortOrder;
    private String icon;
    private Long quantityPermission;
}

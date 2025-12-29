package com.ecommerce.identityservice.dto.response;

import lombok.*;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FunctionDetailDTO {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private Integer sortOrder;
    private String icon;
    private Long quantityPermission;
    private Set<SubFunctionList>  subFunctions;
}

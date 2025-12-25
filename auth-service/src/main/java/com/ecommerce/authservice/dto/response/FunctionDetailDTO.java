package com.ecommerce.authservice.dto.response;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FunctionDetailDTO {
    private String id;
    private String name;
    private String description;
    private Integer sortOrder;
    private String icon;
    private Long quantityPermission;
    private Set<SubFunctionList>  subFunctions;
}

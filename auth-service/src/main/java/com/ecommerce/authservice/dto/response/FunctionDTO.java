package com.ecommerce.authservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FunctionDTO {
    private String id;
    private String name;
    private String description;
    private Integer sortOrder;
    private Long quantityPermission;
}

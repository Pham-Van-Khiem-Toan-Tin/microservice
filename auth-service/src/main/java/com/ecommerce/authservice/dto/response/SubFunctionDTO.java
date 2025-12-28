package com.ecommerce.authservice.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubFunctionDTO {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private Integer sortOrder;
    private FunctionDTO function;
}

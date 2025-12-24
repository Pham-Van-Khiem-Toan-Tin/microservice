package com.ecommerce.authservice.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubFunctionDTO {
    private String id;
    private String name;
    private String description;
    private Integer sortOrder;
    private FunctionDTO function;
}

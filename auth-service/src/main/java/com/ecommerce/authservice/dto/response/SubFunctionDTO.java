package com.ecommerce.authservice.dto.response;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubFunctionDTO {
    private String id;
    private String name;
    private String description;
    private int sortOrder;
}

package com.ecommerce.authservice.dto.response;

import com.ecommerce.authservice.entity.SubFunctionEntity;

import lombok.Builder;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class AllFunctionDTO {
    private String id;
    private String name;
    private String description;
    private int sortOrder;
    private Set<SubFunctionDTO> subFunctions;
}

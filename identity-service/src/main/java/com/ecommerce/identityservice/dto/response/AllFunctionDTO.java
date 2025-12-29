package com.ecommerce.identityservice.dto.response;

import com.ecommerce.identityservice.dto.response.SubFunctionDTO;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class AllFunctionDTO {
    private String id;
    private String code;
    private String name;
    private String description;
    private int sortOrder;
    private Set<SubFunctionDTO> subFunctions;
}

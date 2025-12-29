package com.ecommerce.identityservice.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Builder
public class FunctionEditForm {
    private String id;
    private String code;
    private String name;
    private String description;
    private Integer sortOrder;
    private String icon;
    private Set<SubFunctionForm> subFunctions;
}

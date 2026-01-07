package com.ecommerce.catalogservice.dto.request.attribute;

import lombok.Data;

import java.util.List;

@Data
public class AttributeConfigForm {
    private String id;
    private String code;
    private Boolean isRequired;
    private Boolean isFilterable;
    private Integer displayOrder;
    private List<String> allowedOptionIds;
}

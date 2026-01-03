package com.ecommerce.catalogservice.dto.request;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
public class AttributeConfigForm {
    private String id;
    private boolean isRequired;
    private boolean isFilterable;
    private Integer displayOrder;
    private List<String> allowedOptionIds;
}

package com.ecommerce.catalogservice.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
public class AttributeConfigForm {
    private String id;
    private Boolean isRequired;
    private Boolean isFilterable;
    private Integer displayOrder;
    private List<String> allowedOptionIds;
}

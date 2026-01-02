package com.ecommerce.catalogservice.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class AttributeConfig {
    private String id;
    @Field("is_required")
    private boolean isRequired;
    @Field("is_filterable")
    private boolean isFilterable;
    @Field("display_order")
    private Integer displayOrder;
}

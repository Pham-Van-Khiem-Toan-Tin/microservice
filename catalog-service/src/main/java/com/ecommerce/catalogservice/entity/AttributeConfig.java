package com.ecommerce.catalogservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttributeConfig {
    private String id;
    @Field("is_required")
    private boolean isRequired;
    @Field("is_filterable")
    private boolean isFilterable;
    @Field("display_order")
    private Integer displayOrder;
    @Field("allowed_option_ids")
    private List<String> allowedOptionIds;
}

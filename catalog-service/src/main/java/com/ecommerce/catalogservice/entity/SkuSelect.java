package com.ecommerce.catalogservice.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
public class SkuSelect {
    private String groupId;
    private String valueId;
}

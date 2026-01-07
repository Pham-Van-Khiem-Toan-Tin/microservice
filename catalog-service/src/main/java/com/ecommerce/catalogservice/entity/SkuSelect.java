package com.ecommerce.catalogservice.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class SkuSelect {
    private String code;
    private String valueId;
}

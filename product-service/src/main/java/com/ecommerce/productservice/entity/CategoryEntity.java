package com.ecommerce.productservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("categories")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryEntity {
    private String id;
    private String name;
    private String parentId;
}

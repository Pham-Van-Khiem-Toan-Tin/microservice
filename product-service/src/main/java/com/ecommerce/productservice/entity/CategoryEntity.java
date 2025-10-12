package com.ecommerce.productservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("categories")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CategoryEntity {
    @Id
    private String id;
    private String name;
    private String slug;
    private String parentId;
}

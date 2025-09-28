package com.ecommerce.productservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("variations")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VariationEntity {
    private String name;
    private String categoryId;
}

package com.ecommerce.productservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("variation_option")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VariationOptionEntity {
    private String variationId;
    private String value;
}

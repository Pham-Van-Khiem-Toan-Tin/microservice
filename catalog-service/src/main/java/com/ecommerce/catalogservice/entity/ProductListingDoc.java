package com.ecommerce.catalogservice.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Document(indexName = "product_listing_v1")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListingDoc {
    @Id
    private String productId;

    @Field(type = FieldType.Keyword)
    private String categoryId;

    @Field(type = FieldType.Keyword)
    private List<String> categoryPathIds;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Keyword)
    private String slug;

    @Field(type = FieldType.Keyword)
    private String brandCode;

    // facets.* should be keyword arrays (simple approach: map of string->list)
    @Field(type = FieldType.Object)
    private Map<String, List<String>> facets;

    @Field(type = FieldType.Long)
    private Long minPrice;

    @Field(type = FieldType.Long)
    private Long maxPrice;

    @Field(type = FieldType.Boolean)
    private Boolean inStock;

    @Field(type = FieldType.Date)
    private Instant updatedAt;

}

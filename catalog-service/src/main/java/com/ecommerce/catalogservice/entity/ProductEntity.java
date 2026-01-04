package com.ecommerce.catalogservice.entity;


import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Document("products")
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Data
public class ProductEntity {
    @Id
    private ObjectId id;

    private String name;

    @Indexed(unique = true)
    private String slug;
    @Indexed
    private String brandId;
    @Indexed
    private String categoryId;        // leaf category


    @Indexed
    private Double minPrice;
    @Indexed
    private Double maxPrice;
    private Boolean hasVariants;

    private List<ProductAttribute> attributes;

    private List<ProductOption> options;


    private ImageEntity thumbnail;
    private List<ImageEntity> gallery;
    private String description; // HTML content
    private String shortDescription;


    @Indexed
    private  ProductStatus status;

    private SeoInfo seo;

    private String updatedBy;
    private Instant createdAt;
    private Instant updatedAt;
}

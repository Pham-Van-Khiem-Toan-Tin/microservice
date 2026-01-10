package com.ecommerce.catalogservice.entity;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;

@Document("products")
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Data
@CompoundIndexes({
        @CompoundIndex(name = "idx_variantGroups_id", def = "{'variantGroups.id': 1}"),
        @CompoundIndex(name = "idx_variantGroups_values_value", def = "{'variantGroups.values.id': 1}"),
        @CompoundIndex(name = "idx_variantGroups_specs_code", def = "{'specs.code': 1}"),
        @CompoundIndex(name = "idx_variantGroups_specs_valueId", def = "{'specs.valueId': 1}"),
        @CompoundIndex(name = "idx_variantGroups_specs_valueIds", def = "{'specs.valueIds': 1}")
})
public class ProductEntity {
    @Id
    private String id;

    private String name;

    @Indexed(unique = true)
    private String slug;
    @Indexed
    @Field("brand_id")
    private String brandId;
    @Indexed
    @Field("category_id")
    private String categoryId;        // leaf category

    @Field("has_variant")
    private Boolean hasVariant;

    @Indexed
    @Field("min_price")
    private Double minPrice;
    @Indexed
    @Field("max_price")
    private Double maxPrice;
    private Integer warrantyMonth;
    private List<ProductSpecs> specs;

    private List<ProductVariantGroup> variantGroups;
    @Field("number_of_reviews")
    private Integer numberOfReviews;
    @Field("average_rating")
    private Double averageRating;

    private ImageEntity thumbnail;
    private List<ImageEntity> gallery;
    private String description; // HTML content
    @Field("short_description")
    private String shortDescription;


    @Indexed
    private ProductStatus status;


    private String updatedBy;
    private Instant createdAt;
    private Instant updatedAt;
}

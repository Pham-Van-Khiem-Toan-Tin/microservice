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
    private String categoryId;        // leaf category
    private List<String> ancestors;
    @Indexed
    private String brandCode;

    @Indexed
    private  ProductStatus status;

    private List<SpecBlock> specifications;

    private List<Variant> variants;

    private String updatedBy;
    private Instant createdAt;
    private Instant updatedAt;
}

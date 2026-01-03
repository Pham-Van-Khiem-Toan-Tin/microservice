package com.ecommerce.catalogservice.entity;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;

@Document(collection = "brands")
@Getter
@Setter
@Builder
public class BrandEntity {
    @Id
    private String id;
    @Indexed(unique = true)
    private String name;
    @Indexed(unique = true)
    private String slug;
    private String description;
    private ImageEntity logo;
    @Indexed
    private List<String> categories;

    private BrandStatus status;

    private String updatedBy;
    private Instant updatedAt;
    private Instant createdAt;
}

package com.ecommerce.catalogservice.entity;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("categories")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CategoryEntity {
    @Id
    private ObjectId id;
    private String name;
    private String slug;
    private ObjectId parentId;
    private Integer level;
    private Boolean isLeaf;

    private Boolean isVisible;
    private Integer sortOrder;
    private String menuLabel;
    private String iconUrl;
    private String imageUrl;
    private String menuGroup;
    private Boolean isFeatured;

    private Instant createdAt;
    private Instant updatedAt;
}

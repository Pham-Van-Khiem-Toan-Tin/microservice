package com.ecommerce.catalogservice.entity;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Document("categories")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CategoryEntity {
    @Id
    private String id;
    private String name;
    private String slug;
    @Field("parent_id")
    private String parentId;
    private Integer level;
    private Boolean isLeaf;

    private Boolean isVisible;
    private Integer sortOrder;
    private String menuLabel;
    @Field("icon_url")
    private String iconUrl;
    @Field("image_url")
    private String imageUrl;
    @Field("image_public_id")
    private String imagePublicId;
    private Boolean isFeatured;

    private String updatedBy;
    private Instant createdAt;
    private Instant updatedAt;
}

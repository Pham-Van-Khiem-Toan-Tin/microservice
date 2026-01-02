package com.ecommerce.catalogservice.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;

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
    @Indexed(unique = true)
    private String slug;
    private String icon;
    @Indexed
    @Field("parent_id")
    private String parentId;
    private List<Ancestors> ancestor;
    private Integer level;
    private boolean active;
    private Boolean isLeaf;
    @Field("attribute_configs")
    private List<AttributeConfig> attributeConfigs;
    private ImageEntity image;
    private String updatedBy;
    private Instant createdAt;
    private Instant updatedAt;
}

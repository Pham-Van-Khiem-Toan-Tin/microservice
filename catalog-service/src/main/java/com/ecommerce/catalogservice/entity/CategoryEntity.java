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

@Document("categories")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@CompoundIndexes({
        @CompoundIndex(name = "idx_attr_configs_code", def = "{'attribute_configs.code': 1}")
})
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
    @Indexed
    private boolean active;
    private Boolean isLeaf;
    @Field("attribute_configs")
    private List<AttributeConfig> attributeConfigs;
    private ImageEntity image;
    private String updatedBy;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean deleted;
    private String deletedBy;
    private Instant deletedAt;
}

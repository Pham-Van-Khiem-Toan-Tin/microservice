package com.ecommerce.catalogservice.entity;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document("menu_sections")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MenuSectionEntity {
    @Id
    private ObjectId id;
    private String menuGroup;
    private String name;
    private String rootCategoryId;
    private String title;
    private Integer sortOrder;
    private Set<ObjectId> categoryIds;
}

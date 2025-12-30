package com.ecommerce.catalogservice.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

@Getter
@Setter
public class CategoryUpdateForm {
    private String id;
    private String name;
    private ObjectId parentId;
    private Boolean isVisible;
    private Integer sortOrder;
    private String menuLabel;
    private String iconUrl;
    private String imageUrl;
    private Boolean isFeatured;
}

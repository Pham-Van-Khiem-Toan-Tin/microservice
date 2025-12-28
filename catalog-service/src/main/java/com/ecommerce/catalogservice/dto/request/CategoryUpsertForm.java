package com.ecommerce.catalogservice.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CategoryUpsertForm {
    private String name;
    private String slug;
    private String parentId;     // nullable
    private Boolean isVisible;
    private Integer sortOrder;
    private String menuLabel;
    private String iconUrl;
    private String imageUrl;
    private String menuGroup;
    private Boolean isFeatured;
}

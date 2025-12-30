package com.ecommerce.catalogservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryDetailDTO {
    private String id;
    private String name;
    private boolean isVisible;
    private Integer sortOrder;
    private String menuLabel;
    private String iconUrl;
    private String imageUrl;
    private boolean isFeatured;
    private String parentName;
    private String slug;
}

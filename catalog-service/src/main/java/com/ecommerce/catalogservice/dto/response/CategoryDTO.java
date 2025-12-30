package com.ecommerce.catalogservice.dto.response;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

@Data
@Builder
public class CategoryDTO {
    private String id;
    private String name;
    private String slug;
    private Integer level;
    private Boolean isVisible;
}

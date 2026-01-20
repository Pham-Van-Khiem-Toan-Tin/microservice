package com.ecommerce.catalogservice.dto.response.category;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryDTO {
    private String id;
    private String name;
    private String slug;
    private Integer level;
    private Boolean active;
}

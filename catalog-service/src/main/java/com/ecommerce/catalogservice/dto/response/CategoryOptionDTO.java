package com.ecommerce.catalogservice.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CategoryOptionDTO {
    private String id;
    private String name;
    private Integer level;
    private String parentId;
}

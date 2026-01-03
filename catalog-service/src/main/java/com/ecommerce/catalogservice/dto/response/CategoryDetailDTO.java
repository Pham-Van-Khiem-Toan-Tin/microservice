package com.ecommerce.catalogservice.dto.response;

import com.ecommerce.catalogservice.entity.ImageEntity;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CategoryDetailDTO {
    private String id;
    private String name;
    private String slug;
    private String icon;
    private ImageEntity image;
    private String parentName;
    private String parentId;
    private boolean active;
    private List<AttributeConfigDTO> attributeConfigs;
}

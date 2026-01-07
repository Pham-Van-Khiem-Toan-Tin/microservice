package com.ecommerce.catalogservice.dto.request.category;

import com.ecommerce.catalogservice.dto.request.attribute.AttributeConfigForm;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CategoryCreateForm {
    private String name;
    private String slug;
    private String icon;
    private String parentId;
    private boolean active;
    private List<AttributeConfigForm> attributeConfigs;


}

package com.ecommerce.catalogservice.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class CategoryUpdateForm {
    private String id;
    private String name;
    private String slug;
    private String icon;
    private String parentId;
    private boolean active;
    private List<AttributeConfigForm> attributeConfigs;
}

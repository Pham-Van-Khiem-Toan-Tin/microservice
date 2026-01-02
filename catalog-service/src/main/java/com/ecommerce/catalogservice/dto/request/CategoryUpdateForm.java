package com.ecommerce.catalogservice.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class CategoryUpdateForm {
    private String id;
    private String name;
    private String parentId;
    private Boolean isVisible;
    private Integer sortOrder;
    private String menuLabel;
    private String iconUrl;
    private Boolean isFeatured;

    private MultipartFile image;
}

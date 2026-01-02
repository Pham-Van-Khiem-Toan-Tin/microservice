package com.ecommerce.catalogservice.dto.request;

import com.ecommerce.catalogservice.entity.AttributeConfig;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class CategoryCreateForm {
    private String name;
    private String slug;
    private String icon;
    private String parentId;
    private boolean active;
    private List<AttributeConfig> attributeConfigs;


    private MultipartFile image;
}

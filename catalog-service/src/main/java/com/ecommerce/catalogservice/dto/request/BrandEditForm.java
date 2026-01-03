package com.ecommerce.catalogservice.dto.request;

import com.ecommerce.catalogservice.entity.BrandStatus;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class BrandEditForm {
    private String id;
    private String name;
    private String slug;
    private BrandStatus status;
    private String description;
    private List<String> categories;
}

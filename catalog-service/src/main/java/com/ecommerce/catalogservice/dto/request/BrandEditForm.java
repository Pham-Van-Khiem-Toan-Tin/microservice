package com.ecommerce.catalogservice.dto.request;

import com.ecommerce.catalogservice.entity.BrandStatus;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class BrandEditForm {
    private String id;
    private String name;
    private String code;
    private String slug;
    private BrandStatus status;

    private MultipartFile image;
}

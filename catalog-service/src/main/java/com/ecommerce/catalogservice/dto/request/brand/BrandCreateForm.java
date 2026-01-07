package com.ecommerce.catalogservice.dto.request.brand;

import com.ecommerce.catalogservice.entity.BrandStatus;
import lombok.Data;

import java.util.List;

@Data
public class BrandCreateForm {
    private String name;
    private String slug;
    private BrandStatus status;
    private String description;
    private List<String> categories;
}

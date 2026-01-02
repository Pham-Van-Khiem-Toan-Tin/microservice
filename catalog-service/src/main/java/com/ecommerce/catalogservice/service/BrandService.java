package com.ecommerce.catalogservice.service;

import com.ecommerce.catalogservice.dto.request.BrandCreateForm;
import com.ecommerce.catalogservice.dto.request.BrandEditForm;

public interface BrandService {
    void addBrand(BrandCreateForm form);
    void updateBrand(BrandEditForm form, String id);
    void deleteBrand(String id);
}

package com.ecommerce.catalogservice.service;

import com.ecommerce.catalogservice.dto.request.BrandCreateForm;
import com.ecommerce.catalogservice.dto.request.BrandEditForm;
import com.ecommerce.catalogservice.dto.request.BrandSearchField;
import com.ecommerce.catalogservice.dto.response.BrandDTOS;
import com.ecommerce.catalogservice.dto.response.BrandDetailDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface BrandService {
    Page<BrandDTOS> searchBrands(String keyword, List<BrandSearchField> fields, Pageable pageable);
    BrandDetailDTO getBrand(String id);
    void addBrand(BrandCreateForm form, MultipartFile image);

    void updateBrand(BrandEditForm form, MultipartFile image, String id);

    void deleteBrand(String id);
}

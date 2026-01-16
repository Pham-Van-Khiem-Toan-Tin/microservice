package com.ecommerce.catalogservice.service;

import com.ecommerce.catalogservice.dto.request.brand.BrandCreateForm;
import com.ecommerce.catalogservice.dto.request.brand.BrandEditForm;
import com.ecommerce.catalogservice.dto.request.brand.BrandSearchField;
import com.ecommerce.catalogservice.dto.response.BrandDTOS;
import com.ecommerce.catalogservice.dto.response.BrandDetailDTO;
import com.ecommerce.catalogservice.dto.response.BrandOptionDTOS;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BrandService {
    Page<BrandDTOS> searchBrands(String keyword, List<BrandSearchField> fields, Pageable pageable);
    BrandDetailDTO getBrand(String id);
    void addBrand(BrandCreateForm form, MultipartFile image);

    void updateBrand(BrandEditForm form, MultipartFile image, String idemKey, String id);

    void deleteBrand(String id);
    List<BrandOptionDTOS> getBrandOptions();
    void toggleActiveBrand(String id);
}

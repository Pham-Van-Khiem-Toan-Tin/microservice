package com.ecommerce.catalogservice.service;

import com.ecommerce.catalogservice.constants.Constants;
import com.ecommerce.catalogservice.dto.request.ProductCreateForm;
import com.ecommerce.catalogservice.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    void addProduct(ProductCreateForm form, MultipartFile image, List<MultipartFile> gallery);
}

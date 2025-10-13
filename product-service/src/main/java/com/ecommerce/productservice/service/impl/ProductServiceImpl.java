package com.ecommerce.productservice.service.impl;

import com.ecommerce.productservice.constants.ResponseCode;
import com.ecommerce.productservice.dto.response.ApiResponse;
import com.ecommerce.productservice.repository.ProductRepository;
import com.ecommerce.productservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Override
    public ResponseEntity<ApiResponse<ResponseCode>> createProduct() {

        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<ResponseCode>> updateProduct() {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<ResponseCode>> deleteProduct() {
        return null;
    }
}

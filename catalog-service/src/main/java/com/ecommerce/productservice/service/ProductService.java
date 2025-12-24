package com.ecommerce.productservice.service;

import com.ecommerce.productservice.constants.ResponseCode;
import com.ecommerce.productservice.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;

public interface ProductService {
    ResponseEntity<ApiResponse<ResponseCode>> createProduct();
    ResponseEntity<ApiResponse<ResponseCode>> updateProduct();
    ResponseEntity<ApiResponse<ResponseCode>> deleteProduct();

}

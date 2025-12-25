package com.ecommerce.authservice.config;

import com.ecommerce.authservice.dto.response.ApiResponse;
import com.ecommerce.authservice.dto.response.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        // tùy bạn: mapping status theo code nghiệp vụ, ở đây dùng 400
        log.error(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.of(ex.getResponseCode(), null));
    }
}

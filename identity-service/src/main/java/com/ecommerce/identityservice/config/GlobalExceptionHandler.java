package com.ecommerce.identityservice.config;

import com.ecommerce.identityservice.constants.Constants.*;
import com.ecommerce.identityservice.dto.response.ApiResponse;
import com.ecommerce.identityservice.dto.response.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.ecommerce.identityservice.constants.Constants.*;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Object> handleBusinessException(BusinessException ex) {
        log.warn("Business Error: {}", ex.getConstants().getMessage());
        return ApiResponse.error(ex.getConstants());
    }
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // Lỗi crash server là 500
    public ApiResponse<Object> handleGeneralException(Exception ex) {
        // Log lỗi nghiêm trọng (Stack Trace) để dev fix
        log.error("System Error: ", ex);
        return ApiResponse.error(INTERNAL_SERVER);
    }
}

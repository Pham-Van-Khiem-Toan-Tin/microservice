package com.ecommerce.identityservice.dto.exception;

import com.ecommerce.identityservice.constants.Constants;
import com.ecommerce.identityservice.dto.response.ApiResponse;
import com.ecommerce.identityservice.dto.CustomException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public ApiResponse<String> handleGeneralException(CustomException ex) {
        ApiResponse<String> response = new ApiResponse<>(ex.getStatus(), ex.getMessage());
        return response;
    }
    @ExceptionHandler(Exception.class)
    public ApiResponse<String> handleGeneralException(Exception ex) {
        ex.printStackTrace();
        return new ApiResponse<>(Constants.INTERNAL_SERVER);
    }
}

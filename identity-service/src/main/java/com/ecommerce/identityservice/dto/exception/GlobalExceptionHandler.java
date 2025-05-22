package com.ecommerce.identityservice.dto.exception;

import com.ecommerce.identityservice.constants.Constants;
import com.ecommerce.identityservice.dto.response.ApiResponse;
import jakarta.validation.ConstraintDeclarationException;
import org.springframework.messaging.handler.invocation.MethodArgumentResolutionException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.method.MethodValidationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public ApiResponse<String> handleGeneralException(CustomException ex) {
        ex.printStackTrace();
        ApiResponse<String> response = new ApiResponse<>(ex.getStatus(), ex.getMessage());
        return response;
    }
    @ExceptionHandler(BindException.class)
    public ApiResponse<String> handleValidateException(BindException ex) {
        ex.printStackTrace();
        ApiResponse<String> response = new ApiResponse<>(500, ex.getBindingResult().getFieldError().getDefaultMessage());
        return response;
    }
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ApiResponse<String> handleAccessDenied(AuthorizationDeniedException ex) {
        ex.printStackTrace();
        ApiResponse<String> response = new ApiResponse<>(403, "Bạn không có quyền truy cập tài nguyên này.");
        return response;
    }
    @ExceptionHandler(Exception.class)
    public ApiResponse<String> handleOtherExceptions(Exception ex) {
        ex.printStackTrace();
        return new ApiResponse<>(Constants.INTERNAL_SERVER);
    }
}

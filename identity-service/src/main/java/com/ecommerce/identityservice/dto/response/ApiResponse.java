package com.ecommerce.identityservice.dto.response;

import com.ecommerce.identityservice.constants.Constants;
import lombok.Data;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

public class ApiResponse<T> extends ResponseEntity<T> {
    private int status;
    private T data;

    public ApiResponse(int status, T data) {
        super(data, HttpStatusCode.valueOf(status));
    }

    public ApiResponse(T body, HttpHeaders headers, HttpStatus status) {
        super(body, headers, status);
    }

    public ApiResponse(Constants constants, HttpHeaders headers) {
        super((T) constants.getMessage(), headers, HttpStatus.valueOf(constants.getCode()));
    }

    public ApiResponse(Constants constants) {
        super((T) constants.getMessage(), HttpStatus.valueOf(constants.getCode()));
    }
}

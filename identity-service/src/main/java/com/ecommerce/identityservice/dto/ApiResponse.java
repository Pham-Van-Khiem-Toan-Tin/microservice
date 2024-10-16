package com.ecommerce.identityservice.dto;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private int code;
    private T result;
}

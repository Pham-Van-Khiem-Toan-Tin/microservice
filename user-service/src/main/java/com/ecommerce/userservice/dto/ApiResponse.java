package com.ecommerce.userservice.dto;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private int code;
    private T result;
}

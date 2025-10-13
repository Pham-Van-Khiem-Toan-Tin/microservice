package com.ecommerce.productservice.dto.response;

import com.ecommerce.productservice.constants.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> {
    private int status;
    private String message;
    private T data;
    public static <T> ApiResponse<T> of(ResponseCode responseCode) {
        return ApiResponse.<T>builder()
                .status(responseCode.getStatus())
                .message(responseCode.getMessage())
                .build();
    }

    public static <T> ApiResponse<T> of(ResponseCode code, T data) {
        return ApiResponse.<T>builder()
                .status(code.getStatus())
                .message(code.getMessage())
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> of(int status, T data) {
        return ApiResponse.<T>builder()
                .status(status)
                .data(data)
                .build();
    }
}

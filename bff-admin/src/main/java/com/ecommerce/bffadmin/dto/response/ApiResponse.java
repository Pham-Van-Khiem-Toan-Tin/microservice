package com.ecommerce.bffadmin.dto.response;

import com.ecommerce.bffadmin.constant.Constants;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    public static <T> ApiResponse<T> ok(Constants responseCode, T data) {
        return of(responseCode, data);
    }
    public static <T> ApiResponse<T> ok(Constants responseCode) {
        return of(responseCode, null);
    }
    public static ApiResponse<Void> of(Constants responseCode) {
        return new ApiResponse<>(
                responseCode.getCode(),
                responseCode.getMessage(),
                null
        );
    }
    public static <T> ApiResponse<T> of(Constants code, T data) {
        return new ApiResponse<>(
                code.getCode(),
                code.getMessage(),
                data
        );
    }
}

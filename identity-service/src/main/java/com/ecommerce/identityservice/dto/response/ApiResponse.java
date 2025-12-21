package com.ecommerce.identityservice.dto.response;


import com.ecommerce.identityservice.constants.Constants;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    // Helper tạo response thành công (Mặc định dùng Enum SUCCESS)
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(null)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // 2. Method MỚI: Dùng khi muốn custom message thành công từ Enum
    // Ví dụ: ApiResponse.success(data, Constants.UPDATE_SUCCESS)
    public static <T> ApiResponse<T> success(T data, Constants Constants) {
        return ApiResponse.<T>builder()
                .code(Constants.getCode())
                .message(Constants.getMessage()) // Lấy message cụ thể (VD: "Cập nhật thành công")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // 3. Method MỚI: Dùng khi thành công nhưng KHÔNG CẦN trả về data (VD: Xóa thành công)
    public static <T> ApiResponse<T> success(Constants Constants) {
        return ApiResponse.<T>builder()
                .code(Constants.getCode())
                .message(Constants.getMessage())
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Helper tạo response lỗi từ Enum
    public static <T> ApiResponse<T> error(Constants Constants) {
        return ApiResponse.<T>builder()
                .code(Constants.getCode())
                .message(Constants.getMessage())
                .data(null) // Lỗi thì data thường là null
                .timestamp(LocalDateTime.now())
                .build();
    }
}

package com.ecommerce.inventoryservice.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Constants {
    INTERNAL_ERROR(500, "Không thể xử lí yêu cầu"),
    DATA_FORMAT(400, "Dữ liệu không hợp lệ"),
    VALIDATE_FAIL(4001, "Dữ liệu không hợp lệ."),

    //inventory
    SKU_CODE_ERROR(1009, "Thêm mới sản phẩm thành công");



    private final int code;
    private final String message;

}

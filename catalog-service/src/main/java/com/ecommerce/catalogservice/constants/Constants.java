package com.ecommerce.catalogservice.constants;

import lombok.Getter;

@Getter
public enum Constants {
    INTERNAL_ERROR(500, "Không thể xử lí yêu cầu"),
    DATA_FORMAT(400, "Dữ liệu không hợp lệ"),
    //category
    SLUG_EXIST(409, "Slug đã tồn tại"),

    //product
    UPDATE_PRODUCT_SUCCESS(200, "Cập nhật thông tin sản phẩm thành công"),
    CREATE_PRODUCT_SUCCESS(201, "Thêm mới sản phẩm thành công"),
    DELETE_PRODUCT_SUCCESS(200, "Xoá sản phẩm thành công");

    private final int code;
    private final String message;

    Constants(int code, String message) {
        this.code = code;
        this.message = message;
    }
}

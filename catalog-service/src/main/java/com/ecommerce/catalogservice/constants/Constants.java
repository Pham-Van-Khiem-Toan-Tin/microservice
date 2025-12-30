package com.ecommerce.catalogservice.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Constants {
    INTERNAL_ERROR(500, "Không thể xử lí yêu cầu"),
    DATA_FORMAT(400, "Dữ liệu không hợp lệ"),
    VALIDATE_FAIL(4001, "Dữ liệu không hợp lệ."),

    //category
    SLUG_EXIST(409, "Slug đã tồn tại"),
    CREATE_CATEGORY_FAIL(1002, "Thêm mới danh mục thất bại. Vui lòng thử lại sau"),
    CREATE_CATEGORY_SUCCESS(1001, "Thêm mới danh mục thành công"),
    //product
    UPDATE_PRODUCT_SUCCESS(200, "Cập nhật thông tin sản phẩm thành công"),
    CREATE_PRODUCT_SUCCESS(201, "Thêm mới sản phẩm thành công"),
    DELETE_PRODUCT_SUCCESS(200, "Xoá sản phẩm thành công");

    private final int code;
    private final String message;

}

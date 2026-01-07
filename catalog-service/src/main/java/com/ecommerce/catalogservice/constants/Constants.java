package com.ecommerce.catalogservice.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Constants {
    INTERNAL_ERROR(500, "Không thể xử lí yêu cầu"),
    DATA_FORMAT(400, "Dữ liệu không hợp lệ"),
    VALIDATE_FAIL(4001, "Dữ liệu không hợp lệ."),

    //product
    PRODUCT_CREATE_SUCCESS(1009, "Thêm mới sản phẩm thành công"),

    //brand
    BRAND_CREATE_SUCCESS(1008, "Thêm thương hiệu thành công"),
    BRAND_EDIT_SUCCESS(1007, "Cập nhật thương hiệu thành công"),

    //attribute
    ATTRIBUTE_CREATE_SUCCESS(1006, "Thêm mới thuộc tính thành công."),
    ATTRIBUTE_EDIT_SUCCESS(1006, "Cập nhật thuộc tính thành công."),
    ATTRIBUTE_DELETE_SUCCESS(1006, "Xóa thuộc tính thành công."),


    //category
    SLUG_EXIST(409, "Slug đã tồn tại"),
    DELETE_CATEGORY_SUCESS(1005, "Xóa danh mục thành công."),
    UPDATE_CATEGORY_SUCCESS(1004, "Cập nhật danh mục thành công"),

    UPDATE_CATEGORY_FAIL(1003, "Cập nhật danh mục không thành công"),
    CREATE_CATEGORY_FAIL(1002, "Thêm mới danh mục thất bại. Vui lòng thử lại sau"),
    CREATE_CATEGORY_SUCCESS(1001, "Thêm mới danh mục thành công"),
    //product
    UPDATE_PRODUCT_SUCCESS(200, "Cập nhật thông tin sản phẩm thành công"),
    CREATE_PRODUCT_SUCCESS(201, "Thêm mới sản phẩm thành công"),
    DELETE_PRODUCT_SUCCESS(200, "Xoá sản phẩm thành công");

    private final int code;
    private final String message;

}

package com.ecommerce.orderservice.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Constants {
    INTERNAL_ERROR(500, "Không thể xử lí yêu cầu"),
    ACCESS_DENIED(403, "Không có quyền truy cập tài nguyên"),
    DATA_FORMAT(400, "Dữ liệu không hợp lệ"),
    VALIDATE_FAIL(4001, "Dữ liệu không hợp lệ."),
    REMOVE_CART_SUCCESS(1021, "Xóa sản phẩm thành công."),
    ADD_CART_SUCCESS(1020,"Thêm sản phẩm vào giỏ hàng thành công."),
    UPDATE_ORDER_SUCCESS(200, "Cập nhật đơn hàng sản phẩm thành công"),
    //order
    PAYMENT_ERROR(2010, "Thanh toán thất bại vui lòng thử lại sau"),
    INVENTORY_QUANTITY_FAIL(2009, "Số lượng sản phẩm không đủ để đặt hàng"),
    CANCEL_REASON_REQUIRED(2008, "Thiếu lý do hủy đơn hàng"),
    ORDER_NOT_FOUND(2007, "Không tìm thấy đơn hàng"),
    CREATE_ORDER_SUCCESS(2006, "Đặt hàng thành công"),
    CART_NOT_FOUND(2005, "Không tìm thấy giỏ hàng"),
    ORDER_EVENT_FAIL(2004, "Lỗi xử lý dữ liệu sự kiện"),
    PRODUCT_ERROR(2003, "Một số sản phẩm không đủ tồn kho, vui lòng kiểm tra lại"),
    CART_IS_EMPTY(2002,"Giỏ hàng của bạn đang trống"),
    CART_INVALID(2001, "Không tìm thấy giỏ hàng");

    private final int code;
    private final String message;

}

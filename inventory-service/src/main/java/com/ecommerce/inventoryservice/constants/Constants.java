package com.ecommerce.inventoryservice.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Constants {
    INTERNAL_ERROR(500, "Không thể xử lí yêu cầu"),
    DATA_FORMAT(400, "Dữ liệu không hợp lệ"),
    VALIDATE_FAIL(4001, "Dữ liệu không hợp lệ."),
    //stock
    SERIAL_EXISTED(1015, "Một số Serial không khả dụng để xuất!"),
    STOCK_TOTAL_INCONSISTENT(1014, "Số lượng trong kho không đủ"),

    STOCK_RESERVED_INCONSISTENT(1013, "Không đủ lượng giữ hàng"),
    STOCK_NOT_FOUND(1012, "Mặt hàng không khả dụng"),
    //reservation
    RESERVATION_STATUS_FAIL(1011, "Trạng thái mặt hàng không hợp lệ"),
    RESERVATION_NOT_FOUND(1011, "Không tìm thấy bản ghi giữ hàng"),
    //order
    OUT_OF_STOCK(1010, "Sản phẩm đã hết hàng"),
    //inventory
    SKU_CODE_ERROR(1009, "Thêm mới sản phẩm thành công");



    private final int code;
    private final String message;

}

package com.ecommerce.paymentservice.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Constants {
    INTERNAL_ERROR(500, "Không thể xử lí yêu cầu"),
    DATA_FORMAT(400, "Dữ liệu không hợp lệ"),
    VALIDATE_FAIL(4001, "Dữ liệu không hợp lệ."),
    //wallet
    WALLET_PAY_SUCCESS(40003, "Thanh toán thành công"),
    WALLET_INVALID(40002, "Ví không tồn tại"),
    WALLET_ENOUGH(40003, "Số dư ví không đủ"),
    //qr
    QR_CREATE_SUCCESS(40001, "Tạo mã qr thành công");


    private final int code;
    private final String message;

}

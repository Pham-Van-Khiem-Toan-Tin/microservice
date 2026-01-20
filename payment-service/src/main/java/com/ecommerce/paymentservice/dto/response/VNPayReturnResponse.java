package com.ecommerce.paymentservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class VNPayReturnResponse {
    private int result; // 1: Thành công, 0: Thất bại, -1: Sai chữ ký
    private String type; // ORDER hoặc DEPOSIT
    private String referenceId; // Mã đơn hàng hoặc ID ví
}

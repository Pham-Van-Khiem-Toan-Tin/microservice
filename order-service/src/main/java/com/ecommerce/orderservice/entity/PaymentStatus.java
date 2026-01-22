package com.ecommerce.orderservice.entity;

public enum PaymentStatus {
    INIT,        // tạo payment record
    PENDING,     // đã gửi sang cổng / chờ IPN
    SUCCEEDED,   // thanh toán thành công
    FAILED,      // thanh toán thất bại
    REFUNDED     // đã hoàn tiền
}

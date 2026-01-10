package com.ecommerce.orderservice.entity;

public enum OrderStatus {
    PENDING,        // Mới tạo, chờ thanh toán hoặc xác nhận
    CONFIRMED,      // Đã xác nhận (Kho đã trừ)
    SHIPPING,       // Đang giao
    DELIVERED,      // Giao thành công
    CANCELLED,      // Đã hủy
    RETURNED        // Trả hàng
}

package com.ecommerce.orderservice.entity;

public enum OrderStatus {
    CREATED,            // Tạo order, chưa giữ hàng
    RESERVED,           // Đã giữ hàng (Inventory.Reserved)
    AWAITING_PAYMENT,   // Chờ thanh toán
    PAID,               // Thanh toán thành công
    CONFIRMED,          // Kho đã trừ / đã xuất (commit stock)
    SHIPPING,           // Đang giao
    DELIVERED,          // Giao thành công
    COMPLETED,          // Hoàn tất nghiệp vụ (hết thời gian đổi trả)
    CANCELLED,          // Hủy trước khi ship
    EXPIRED,            // Hết hạn thanh toán/giữ hàng
    RETURNED            // Trả hàng
}

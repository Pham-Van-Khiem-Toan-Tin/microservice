package com.ecommerce.orderservice.dto.response.order;

import com.ecommerce.orderservice.entity.PaymentMethod;
import com.ecommerce.orderservice.entity.PaymentStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailsResponse {
    private String orderId;
    private String orderNumber;      // Mã đơn hàng (ORD-...)
    private LocalDateTime createdAt; // Thời gian đặt hàng
    private String status;           // PENDING, PAID, CONFIRMED...

    // Thông tin thanh toán
    private BigDecimal finalAmount;  // Tổng tiền cuối cùng khách phải trả
    private PaymentMethod paymentMethod;    // VNPAY, COD, WALLET
    private PaymentStatus paymentStatus;    // UNPAID, PAID

    // Thông tin nhận hàng (Rút gọn)
    private String receiverName;
    private String phoneNumber;
    private String fullAddress;      // Nối các trường địa chỉ thành 1 chuỗi

    // Danh sách sản phẩm (Để hiển thị tóm tắt)
    private List<OrderItemResponse> items;

    @Data
    @Builder
    public static class OrderItemResponse {
        private String orderItemId;
        private String productId;
        private String skuCode;
        private String skuId;
        private String productName;
        private String variantName;
        private int quantity;
        private BigDecimal unitPrice;
        private String thumbnail;
        private Boolean reviewed;
    }
}

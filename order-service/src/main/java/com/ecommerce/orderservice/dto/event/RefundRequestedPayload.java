package com.ecommerce.orderservice.dto.event;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequestedPayload {
    private boolean refund;
    private String orderId;
    private String orderNumber;
    private BigDecimal amount;        // hoặc Long nếu bạn lưu tiền kiểu long
    private String reason;            // "LATE_PAYMENT"
    private String transactionNo;     // mã giao dịch VNPAY để đối soát
    private LocalDateTime requestedAt;
}

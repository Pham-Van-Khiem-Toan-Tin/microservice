package com.ecommerce.paymentservice.enums;

public enum TransactionType {
    DEPOSIT,    // Nạp tiền vào ví
    PAYMENT,    // Thanh toán đơn hàng (Trừ tiền)
    REFUND      // Hoàn tiền (Cộng tiền)
}

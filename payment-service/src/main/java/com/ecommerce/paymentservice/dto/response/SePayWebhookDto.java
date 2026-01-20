package com.ecommerce.paymentservice.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SePayWebhookDto {
    private Long id;                          // ID giao dịch SePay (Quan trọng để check trùng)
    private String gateway;                   // VD: Vietcombank
    private String transactionDate;           // Thời gian GD
    private String accountNumber;             // Số TK người nhận
    private String code;                      // Mã thanh toán (có thể null)
    private String content;                   // Nội dung CK (Quan trọng: TZNAP 101)
    private String transferType;              // "in" hoặc "out"
    private BigDecimal transferAmount;        // Số tiền
    private BigDecimal accumulated;           // Số dư lũy kế (Optional)
    private String subAccount;                // Tài khoản phụ
    private String referenceCode;             // Mã tham chiếu ngân hàng
    private String description;               // Full tin nhắn SMS
}

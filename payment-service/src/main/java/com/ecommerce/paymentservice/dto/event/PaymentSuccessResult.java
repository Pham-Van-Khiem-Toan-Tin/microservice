package com.ecommerce.paymentservice.dto.event;

import com.ecommerce.paymentservice.enums.PaymentMethod;
import com.ecommerce.paymentservice.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor // Bắt buộc phải có để Jackson Deserialization hoạt động
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // Tránh lỗi nếu các service khác phiên bản DTO
public class PaymentSuccessResult implements Serializable {

    private String orderNumber;     // hoặc orderId
    private BigDecimal amount;
    private String providerRef;     // vnp_TransactionNo hoặc sepay id
    private PaymentMethod method;          // VNPAY | SEPAY | WALLET
    private LocalDateTime paidAt;
}
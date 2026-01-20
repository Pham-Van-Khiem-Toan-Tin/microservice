package com.ecommerce.paymentservice.dto.event;

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
public class PaymentSuccessEvent implements Serializable {

    private String orderNumber;      // Mã đơn hàng (reference_id)

    private BigDecimal amount;       // Số tiền đã thanh toán thực tế

    private String transactionNo;    // Mã giao dịch từ phía VNPAY/Ngân hàng (external_trans_id)

    private String paymentMethod;    // VNPAY, QR_CODE, v.v.

    private PaymentStatus paymentStatus;    // Thường là "PAID"

    private LocalDateTime paidAt;    // Thời điểm thanh toán thành công
}
package com.ecommerce.inventoryservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor // Cực kỳ quan trọng để Jackson không báo lỗi
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // Giúp hệ thống không bị crash nếu sau này bạn thêm trường mới
public class StockResultPayload implements Serializable {

    private String orderId;

    private String userId;

    /**
     * Trạng thái xử lý kho: SUCCESS hoặc FAILED
     */
    private String status;

    /**
     * Lý do thất bại (nếu có), ví dụ: "Sản phẩm SKU-123 đã hết hàng"
     */
    private String reason;
}

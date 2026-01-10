package com.ecommerce.catalogservice.dto.request.product;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductCreatedEvent {
    private String productId;       // ID tham chiếu bên MongoDB (để trace log)
    private List<SkuInitData> skus; // Danh sách SKU cần tạo kho

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SkuInitData {
        private String skuCode;
        private Integer initialStock;
    }
}

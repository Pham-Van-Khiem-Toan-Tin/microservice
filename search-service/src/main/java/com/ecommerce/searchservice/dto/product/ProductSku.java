package com.ecommerce.searchservice.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSku {
    private String skuId;
    private String skuCode;
    private String name;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private BigDecimal costPrice; // Lưu ý: Chỉ trả ra nếu là role Admin
    private ImageRef thumbnail;
    private List<SkuSelection> selections;
    private Boolean active;
    private String status;
}

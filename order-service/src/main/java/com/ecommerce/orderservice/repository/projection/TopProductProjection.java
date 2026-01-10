package com.ecommerce.orderservice.repository.projection;

import java.math.BigDecimal;

public interface TopProductProjection {
    String getProductId();
    String getProductName();
    Long getQuantity();
    BigDecimal getRevenue();
}

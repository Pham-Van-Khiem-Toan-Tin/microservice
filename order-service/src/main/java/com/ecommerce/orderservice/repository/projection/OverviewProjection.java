package com.ecommerce.orderservice.repository.projection;

import java.math.BigDecimal;

public interface OverviewProjection {
    Long getTotalOrders();
    Long getDeliveredOrders();
    Long getCancelledOrders();

    BigDecimal getRevenue();
    BigDecimal getGrossAmount();
    BigDecimal getDiscountAmount();

    Long getItemsSold();

    Long getCodOrders();
    Long getVnpayOrders();
    Long getBankTransferOrders();
}

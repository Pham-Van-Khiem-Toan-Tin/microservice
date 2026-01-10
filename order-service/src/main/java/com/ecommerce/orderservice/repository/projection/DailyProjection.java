package com.ecommerce.orderservice.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DailyProjection {
    LocalDate getStatDate();
    Long getTotalOrders();
    BigDecimal getRevenue();
}
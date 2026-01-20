package com.ecommerce.orderservice.service.impl;

import com.ecommerce.orderservice.entity.OrderEntity;
import com.ecommerce.orderservice.entity.OrderItemEntity;
import com.ecommerce.orderservice.entity.OrderStatsDaily;
import com.ecommerce.orderservice.entity.PaymentMethod;
import com.ecommerce.orderservice.repository.OrderProductStatsDailyRepository;
import com.ecommerce.orderservice.repository.OrderStatsDailyRepository;
import com.ecommerce.orderservice.service.OrderStatisticsService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Service
public class OrderStatisticsServiceImpl implements OrderStatisticsService {
    @Autowired
    private OrderProductStatsDailyRepository productRepo;
    @Autowired
    private OrderStatsDailyRepository dailyRepo;
    @Async // Nên chạy bất đồng bộ để không làm chậm quá trình đặt hàng
    @Transactional
    @Override
    public void updateStatsOnNewOrder(OrderEntity order) {
        LocalDate today = order.getCreatedAt().toLocalDate();

        // 1. Xử lý OrderStatsDaily
        OrderStatsDaily daily = OrderStatsDaily.builder()
                .statDate(today)
                .totalOrders(1L)
                .revenue(order.getFinalAmount()) // Sau chiết khấu
                .grossAmount(order.getSubTotalAmount()) // Trước chiết khấu
                .discountAmount(Objects.requireNonNullElse(order.getDiscountAmount(), BigDecimal.ZERO))
                .itemsSold((long) order.getOrderItems().size())
                .codOrders(order.getPaymentMethod() == PaymentMethod.COD ? 1L : 0L)
                .vnpayOrders(order.getPaymentMethod() == PaymentMethod.VNPAY ? 1L : 0L)
                .bankTransferOrders(order.getPaymentMethod() == PaymentMethod.BANK ? 1L : 0L)
                .build();

        dailyRepo.upsertStats(daily);

        // 2. Xử lý OrderProductStatsDaily (Lặp qua từng item)
        for (OrderItemEntity item : order.getOrderItems()) {
            productRepo.upsertProductStats(
                    today,
                    item.getProductId(),
                    item.getProductName(),
                    (long) item.getQuantity(),
                    item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
            );
        }
    }
}

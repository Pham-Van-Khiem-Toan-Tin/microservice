package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dto.response.OrderDailyStatsDTO;
import com.ecommerce.orderservice.dto.response.OrderStatsOverviewDTO;
import com.ecommerce.orderservice.dto.response.TopProductDailyStatsDTO;
import com.ecommerce.orderservice.repository.OrderProductStatsDailyRepository;
import com.ecommerce.orderservice.repository.OrderStatsDailyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class OrderStatsService {
    @Autowired
    private OrderStatsDailyRepository statsDailyRepo;
    @Autowired
    private OrderProductStatsDailyRepository productStatsDailyRepo;
    public OrderStatsOverviewDTO getOverview(LocalDate from, LocalDate to) {
        var p = statsDailyRepo.overview(from, to);

        // defensive null handling
        return OrderStatsOverviewDTO.builder()
                .totalOrders(nvl(p.getTotalOrders()))
                .deliveredOrders(nvl(p.getDeliveredOrders()))
                .cancelledOrders(nvl(p.getCancelledOrders()))
                .revenue(nvlBd(p.getRevenue()))
                .grossAmount(nvlBd(p.getGrossAmount()))
                .discountAmount(nvlBd(p.getDiscountAmount()))
                .itemsSold(nvl(p.getItemsSold()))
                .codOrders(nvl(p.getCodOrders()))
                .vnpayOrders(nvl(p.getVnpayOrders()))
                .bankTransferOrders(nvl(p.getBankTransferOrders()))
                .build();
    }

    public List<OrderDailyStatsDTO> getDaily(LocalDate from, LocalDate to) {
        return statsDailyRepo.daily(from, to).stream()
                .map(p -> OrderDailyStatsDTO.builder()
                        .statDate(p.getStatDate())
                        .totalOrders(nvl(p.getTotalOrders()))
                        .revenue(nvlBd(p.getRevenue()))
                        .build())
                .toList();
    }

    public List<TopProductDailyStatsDTO> getTopProducts(LocalDate from, LocalDate to, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100)); // chống spam
        return productStatsDailyRepo.topProducts(from, to, safeLimit).stream()
                .map(p -> TopProductDailyStatsDTO.builder()
                        .productId(p.getProductId())
                        .productName(p.getProductName())
                        .quantity(nvl(p.getQuantity()))
                        .revenue(nvlBd(p.getRevenue()))
                        .build())
                .toList();
    }

    private static long nvl(Long v) {
        return v == null ? 0L : v;
    }

    private static BigDecimal nvlBd(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}

package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.dto.response.ApiResponse;
import com.ecommerce.orderservice.dto.response.OrderDailyStatsDTO;
import com.ecommerce.orderservice.dto.response.OrderStatsOverviewDTO;
import com.ecommerce.orderservice.dto.response.TopProductDailyStatsDTO;
import com.ecommerce.orderservice.service.OrderStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class OrderStatsController {

    private final OrderStatsService service;

    @GetMapping("/overview")
    public OrderStatsOverviewDTO overview(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return service.getOverview(from, to);
    }

    @GetMapping("/daily")
    public List<OrderDailyStatsDTO> daily(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return service.getDaily(from, to);
    }

    @GetMapping("/top-products")
    public List<TopProductDailyStatsDTO> topProducts(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return service.getTopProducts(from, to, limit);
    }
}

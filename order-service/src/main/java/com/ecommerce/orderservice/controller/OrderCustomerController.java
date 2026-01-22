package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.dto.request.OrderCreateForm;
import com.ecommerce.orderservice.dto.response.ApiResponse;
import com.ecommerce.orderservice.dto.response.order.OrderCustomerResponse;
import com.ecommerce.orderservice.dto.response.order.OrderDetailsResponse;
import com.ecommerce.orderservice.dto.response.order.OrderResponse;
import com.ecommerce.orderservice.service.OrderService;
import com.ecommerce.orderservice.service.SseService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static com.ecommerce.orderservice.constants.Constants.CREATE_ORDER_SUCCESS;

@RestController
@RequestMapping("/customer-order")
public class OrderCustomerController {
    @Autowired
    OrderService orderService;
    @Autowired
    SseService sseService;
    @PostMapping
    public OrderResponse createOrder(HttpServletRequest req, @RequestBody OrderCreateForm form) {

        return orderService.createOrder(req, form);
    }
    @GetMapping("/number/{orderNumber}")
    public OrderDetailsResponse getOrderByNumber(@PathVariable String orderNumber) {
        return orderService.getOrderByNumber(orderNumber);
    }
    @GetMapping("/my-orders")
    public Page<OrderCustomerResponse> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderService.getMyOrders(pageable);
    }
    @GetMapping(path = "/sse/{orderNumber}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable String orderNumber) {
        // Nếu bạn dùng Cookie Auth, Spring Security sẽ tự kiểm tra ở đây
        return sseService.subscribe(orderNumber);
    }
}

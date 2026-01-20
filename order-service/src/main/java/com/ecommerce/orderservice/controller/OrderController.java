package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.dto.request.OrderCreateForm;
import com.ecommerce.orderservice.dto.request.UpdateOrderRequest;
import com.ecommerce.orderservice.dto.response.ApiResponse;
import com.ecommerce.orderservice.dto.response.OrderDTO;
import com.ecommerce.orderservice.dto.response.OrderDetailDTO;
import com.ecommerce.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.ecommerce.orderservice.constants.Constants.*;

@RestController
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    OrderService orderService;
    @PreAuthorize("hasAuthority('VIEW_ORDER_LIST')")
    @GetMapping
    public Page<OrderDTO> all(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(required = false, name = "fields") List<String> fields,
                       @RequestParam(defaultValue = "order_number:asc") String sort,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size) {
        return orderService.search(keyword, fields, sort, page, size);
    }
    @PreAuthorize("hasAuthority('EDIT_ORDER')")
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateOrder(@RequestBody UpdateOrderRequest req, @PathVariable String id) {
        orderService.updateOrder(id, req);
        return ApiResponse.ok(UPDATE_ORDER_SUCCESS);
    }
    @PreAuthorize("hasAuthority('VIEW_MY_ORDER')")
    @GetMapping("/me")
    public List<OrderDTO> allMyOrders() {
        return orderService.findAllMyOrder();
    }
    @PreAuthorize("hasAuthority('VIEW_MY_ORDER')")
    @GetMapping("/me/{id}")
    public OrderDetailDTO myOrderDetails(@PathVariable String id) {
        return orderService.findMyOrderDetail(id);
    }
    @PreAuthorize("hasAuthority('VIEW_ORDER')")
    @GetMapping("/view/{id}")
    public OrderDetailDTO viewOrder(@PathVariable String id) {
        return orderService.findOrderDetail(id);
    }

}

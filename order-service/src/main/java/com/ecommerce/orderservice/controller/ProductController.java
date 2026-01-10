package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.dto.response.OrderExistenceDTO;
import com.ecommerce.orderservice.service.MessageProducer;
import com.ecommerce.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController  {
    @Autowired
    private OrderService orderService;
    @PreAuthorize("hasAuthority('DELETE_PRODUCT')")
    @GetMapping("/{id}/order-usage")
    public OrderExistenceDTO checkProduct(@PathVariable String id) {
        return orderService.findProductExist(id);
    }
}

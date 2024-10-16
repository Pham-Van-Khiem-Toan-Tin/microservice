package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.service.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {
    @Autowired
    MessageProducer messageProducer;
    @GetMapping( "/api/order")
    public String getOrder() {
        System.out.println("chay vao day");
        return "test";
    }
    @GetMapping("/send")
    public String sendMessage() {
        messageProducer.sendMessage("message from order");
        return "messageSend";
    }
}

package com.ecommerce.orderservice;

import com.ecommerce.orderservice.config.FeignClientConfig;
import com.netflix.discovery.EurekaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Lazy;

@SpringBootApplication
@EnableFeignClients(defaultConfiguration = FeignClientConfig.class)
public class OrderServiceApplication {
    @Autowired
    @Lazy
    private EurekaClient eurekaClient;

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

}

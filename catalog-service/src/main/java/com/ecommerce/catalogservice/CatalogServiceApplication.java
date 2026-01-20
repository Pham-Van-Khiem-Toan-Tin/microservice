package com.ecommerce.catalogservice;

import com.ecommerce.catalogservice.config.FeignClientConfig;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Lazy;

@SpringBootApplication
@EnableFeignClients(defaultConfiguration = FeignClientConfig.class)
public class CatalogServiceApplication {
    @Autowired
    @Lazy
    private EurekaClient eurekaClient;
    public static void main(String[] args) {
        SpringApplication.run(CatalogServiceApplication.class, args);
    }
}

package com.ecommerce.authservice;

import com.netflix.discovery.EurekaClient;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Lazy;

import java.util.TimeZone;

@SpringBootApplication
public class AuthServiceApplication {
    @Autowired
    @Lazy
    private EurekaClient eurekaClient;
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}

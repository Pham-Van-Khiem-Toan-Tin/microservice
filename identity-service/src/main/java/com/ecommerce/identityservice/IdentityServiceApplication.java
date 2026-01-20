package com.ecommerce.identityservice;

import com.ecommerce.identityservice.config.FeignClientConfig;
import com.netflix.discovery.EurekaClient;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.client.RestTemplate;

import java.util.TimeZone;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableFeignClients(defaultConfiguration = FeignClientConfig.class)
public class IdentityServiceApplication {
    @Autowired
    @Lazy
    private EurekaClient eurekaClient;
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }
    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }


}

package com.ecommerce.orderservice.integration;

import com.ecommerce.orderservice.dto.response.AddressDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "identity-service",
        url = "${application.config.identity-url}")
public interface IdentityFeignClient {
    @GetMapping("/user-addresses/addresses/{id}")
    AddressDTO getAddressById(@PathVariable("id") String id);
}

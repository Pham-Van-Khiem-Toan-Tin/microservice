package com.ecommerce.orderservice.integration;

import com.ecommerce.orderservice.dto.response.cart.SkuCartDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "catalog-service",
        url = "${application.config.catalog-url}"
)
public interface CatalogFeignClient {
    @PostMapping("/skus/list-info")
    List<SkuCartDTO> getSkuDetailsBatch(@RequestBody List<String> skuIds);
}

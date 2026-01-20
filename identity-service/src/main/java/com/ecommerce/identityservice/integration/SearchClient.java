package com.ecommerce.identityservice.integration;

import com.ecommerce.identityservice.dto.response.ProductListItemResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "search-service", url = "${application.config.search-url}")
public interface SearchClient {

    @PostMapping("/products-by-ids")
    List<ProductListItemResponse> getProductsByIds(@RequestBody List<String> productIds);
}

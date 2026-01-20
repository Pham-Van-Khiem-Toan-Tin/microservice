package com.ecommerce.catalogservice.service;

import com.ecommerce.catalogservice.dto.response.cart.SkuCartDTO;

import java.util.List;

public interface SkuService {
    List<SkuCartDTO> getSkuCartDTO(List<String> skuIds);
}

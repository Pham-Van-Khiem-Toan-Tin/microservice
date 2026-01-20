package com.ecommerce.catalogservice.controller;

import com.ecommerce.catalogservice.dto.response.cart.SkuCartDTO;
import com.ecommerce.catalogservice.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/skus")
public class SkuController {
    @Autowired
    private SkuService skuService;
    @PreAuthorize("hasAuthority('VIEW_CART')")
    @PostMapping("/list-info")
    public List<SkuCartDTO> getSkuDetailsBatch(@RequestBody List<String> skuIds) {
        return skuService.getSkuCartDTO(skuIds);
    }
}

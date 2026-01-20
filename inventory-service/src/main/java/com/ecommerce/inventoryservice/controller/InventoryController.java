package com.ecommerce.inventoryservice.controller;

import com.ecommerce.inventoryservice.dto.request.InventoryAdjustRequest;
import com.ecommerce.inventoryservice.dto.request.OrderItemCheckForm;
import com.ecommerce.inventoryservice.dto.response.InventoryAdjustResponse;
import com.ecommerce.inventoryservice.dto.response.InventoryDTO;
import com.ecommerce.inventoryservice.dto.response.cart.InventoryCartDto;
import com.ecommerce.inventoryservice.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventories")
public class InventoryController {
    @Autowired
    InventoryService inventoryService;
    @PreAuthorize("hasAuthority('VIEW_INVENTORY_LIST')")
    @GetMapping
    public Page<InventoryDTO> search(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false, name = "fields") List<String> fields,
            @RequestParam(defaultValue = "skuCode:asc") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return inventoryService.search(keyword, fields, sort, page, size);
    }
    @PreAuthorize("hasAuthority('EDIT_INVENTORY')")
    @PostMapping("/adjust")
    public InventoryAdjustResponse adjust(@RequestBody InventoryAdjustRequest form) {
        return inventoryService.adjustInventory(form);
    }
    @PreAuthorize("hasAuthority('VIEW_CART')")
    @PostMapping("/batch-check")
    public List<InventoryCartDto> getStockBySkuIds(@RequestBody List<String> skuCodes) {
        return inventoryService.getStockOfSkus(skuCodes);
    }
    @PostMapping("/check-availability")
    public boolean checkAvailability(@RequestBody List<OrderItemCheckForm> items) {
        return inventoryService.checkAvailability(items);
    }
}


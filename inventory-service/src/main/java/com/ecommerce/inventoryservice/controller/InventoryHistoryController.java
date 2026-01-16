package com.ecommerce.inventoryservice.controller;

import com.ecommerce.inventoryservice.dto.response.InventoryHistoryDTO;
import com.ecommerce.inventoryservice.entity.InventoryType;
import com.ecommerce.inventoryservice.service.InventoryHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/inventory")
public class InventoryHistoryController {
    @Autowired
    private InventoryHistoryService inventoryHistoryService;
    @GetMapping("/{skuCode}/history")
    public Page<InventoryHistoryDTO> history(
            @PathVariable String skuCode,
            @RequestParam(required = false) InventoryType type,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return inventoryHistoryService.getHistory(skuCode, type, from, to, page, size);
    }
}

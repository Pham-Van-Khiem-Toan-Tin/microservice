package com.ecommerce.inventoryservice.service;

import com.ecommerce.inventoryservice.dto.response.InventoryHistoryDTO;
import com.ecommerce.inventoryservice.entity.InventoryType;
import org.springframework.data.domain.Page;

import java.time.Instant;

public interface InventoryHistoryService {
    Page<InventoryHistoryDTO> getHistory(
            String skuCode,
            InventoryType type,
            Instant from,
            Instant to,
            int page,
            int size
    );

}

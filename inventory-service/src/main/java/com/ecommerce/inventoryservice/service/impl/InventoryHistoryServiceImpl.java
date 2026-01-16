package com.ecommerce.inventoryservice.service.impl;

import com.ecommerce.inventoryservice.dto.response.InventoryHistoryDTO;
import com.ecommerce.inventoryservice.entity.InventoryHistoryEntity;
import com.ecommerce.inventoryservice.entity.InventoryType;
import com.ecommerce.inventoryservice.repository.InventoryHistoryRepository;
import com.ecommerce.inventoryservice.service.InventoryHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class InventoryHistoryServiceImpl implements InventoryHistoryService {
    @Autowired
    private InventoryHistoryRepository inventoryHistoryRepository;

    @Override
    public Page<InventoryHistoryDTO> getHistory(String skuCode, InventoryType type, Instant from, Instant to, int page, int size) {
        if (skuCode == null || skuCode.isBlank()) {
            throw new IllegalArgumentException("skuCode is required");
        }

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.max(size, 1),
                Sort.by(Sort.Direction.DESC, "createdAt") // mới nhất trước
        );

        boolean hasType = type != null;
        boolean hasRange = from != null && to != null;

        Page<InventoryHistoryEntity> entityPage;

        if (hasType && hasRange) {
            entityPage = inventoryHistoryRepository.findBySkuCodeAndTypeAndCreatedAtBetween(skuCode.trim(), type, from, to, pageable);
        } else if (hasType) {
            entityPage = inventoryHistoryRepository.findBySkuCodeAndType(skuCode.trim(), type, pageable);
        } else if (hasRange) {
            entityPage = inventoryHistoryRepository.findBySkuCodeAndCreatedAtBetween(skuCode.trim(), from, to, pageable);
        } else {
            entityPage = inventoryHistoryRepository.findBySkuCode(skuCode.trim(), pageable);
        }

        return entityPage.map(this::toDTO);
    }

    private InventoryHistoryDTO toDTO(InventoryHistoryEntity h) {
        return InventoryHistoryDTO.builder()
                .id(h.getId().toString())
                .skuCode(h.getSkuCode())
                .quantityChange(h.getQuantityChange())
                .stockAfter(h.getStockAfter())
                .type(h.getType())
                .referenceId(h.getReferenceId())
                .createdAt(h.getCreatedAt())
                .build();
    }
}

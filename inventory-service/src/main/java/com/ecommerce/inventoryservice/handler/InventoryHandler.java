package com.ecommerce.inventoryservice.handler;

import com.ecommerce.inventoryservice.dto.event.PaymentFailedResult;
import com.ecommerce.inventoryservice.dto.event.PaymentSuccessResult;
import com.ecommerce.inventoryservice.entity.InventoryItemEntity;
import com.ecommerce.inventoryservice.entity.InventoryItemStatus;
import com.ecommerce.inventoryservice.repository.InventoryItemRepository;
import com.ecommerce.inventoryservice.repository.InventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InventoryHandler {
    @Autowired
    private InventoryItemRepository inventoryItemRepo;
    @Autowired
    private InventoryRepository  inventoryRepo;
    public void handlePaySucceeded(PaymentSuccessResult payload) {
        MDC.put("sagaStep", "INVENTORY_COMMIT_LOGIC");
        log.info("Bắt đầu xử lý chốt kho cho đơn hàng: {}", payload.getOrderNumber());
        List<InventoryItemEntity> items = inventoryItemRepo.findAllByOrderNumber(payload.getOrderNumber());
        if (items.isEmpty()) {
            MDC.put("status", "NOT_FOUND");
            log.warn("Không tìm thấy bất kỳ Serial nào đang được giữ cho đơn hàng này!");
            return;
        }
        boolean allReserved = items.stream()
                .allMatch(i -> i.getStatus() == InventoryItemStatus.RESERVED);
        if (allReserved) {
            log.info("Hàng vẫn đang được giữ (RESERVED). Tiến hành hạ kho...");
            commitStock(payload.getOrderNumber(), items);
            // HAPPY PATH: Tiền về khi hàng vẫn đang được giữ
            commitStock(payload.getOrderNumber(), items);
        } else {
            MDC.put("status", "ALREADY_PROCESSED");
            log.info("Hàng đã ở trạng thái khác (SOLD/CANCELLED), bỏ qua xử lý.");
        }
    }
    private void commitStock(String orderNumber, List<InventoryItemEntity> items) {
        try {
            // 1. Nhóm theo SKU để trừ kho tổng
            Map<String, Long> skuCounts = items.stream()
                    .collect(Collectors.groupingBy(InventoryItemEntity::getSkuCode, Collectors.counting()));

            // 2. Trừ kho tổng (Giảm total_stock và giảm cả reserved_stock)
            skuCounts.forEach((skuCode, count) -> {
                log.info("Hạ kho SKU: {} | Số lượng: {}", skuCode, count);
                inventoryRepo.confirmStock(skuCode, count.intValue());
            });

            // 3. Chuyển trạng thái Serial sang SOLD
            items.forEach(item -> {
                item.setStatus(InventoryItemStatus.SOLD);
            });
            inventoryItemRepo.saveAll(items);
            MDC.put("status", "SUCCESS");
            log.info("Đã CHỐT KHO (SOLD) thành công cho đơn hàng: {}", orderNumber);
        } catch (Exception e) {
            MDC.put("status", "FAILED");
            log.error("Lỗi nghiêm trọng khi hạ kho: {}", e.getMessage());
            throw e;
        }

    }
}

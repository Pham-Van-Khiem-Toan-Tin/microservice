package com.ecommerce.inventoryservice.config;

import com.ecommerce.inventoryservice.entity.InventoryItemEntity;
import com.ecommerce.inventoryservice.entity.InventoryItemStatus;
import com.ecommerce.inventoryservice.repository.InventoryItemRepository;
import com.ecommerce.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class InventoryCleanupScheduler {

    private final InventoryItemRepository itemRepo;
    private final InventoryRepository inventoryRepo;
    // private final KafkaTemplate<String, Object> kafkaTemplate; // Nếu bạn muốn báo cho Order Service

    /**
     * Chạy mỗi 1 phút (60,000 milliseconds)
     * Bạn có thể dùng cron expression: @Scheduled(cron = "0 * * * * *")
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupExpiredReservations() {
        log.info("Bắt đầu quét kho dọn dẹp hàng giữ quá hạn...");

        // 1. Xác định mốc thời gian hết hạn (ví dụ: hiện tại - 15 phút)
        LocalDateTime timeout = LocalDateTime.now().minusMinutes(15);

        // 2. Tìm tất cả Serial đang RESERVED mà đã quá 15 phút
        List<InventoryItemEntity> expiredItems = itemRepo.findAllByStatusAndReservedAtBefore(
                InventoryItemStatus.RESERVED, timeout);

        if (expiredItems.isEmpty()) {
            return;
        }

        log.info("Phát hiện {} Serial quá hạn thanh toán. Tiến hành nhả hàng...", expiredItems.size());

        // 3. Nhóm theo SKU để cập nhật số lượng ở bảng tổng 1 lần duy nhất (Tránh N+1)
        Map<String, Long> skuCounts = expiredItems.stream()
                .collect(Collectors.groupingBy(InventoryItemEntity::getSkuCode, Collectors.counting()));

        // 4. Cập nhật bảng Inventory tổng: Giảm reserved_stock
        skuCounts.forEach((skuCode, count) -> {
            log.info("Nhả {} cái cho SKU: {}", count, skuCode);
            inventoryRepo.releaseReservedStock(skuCode, count.intValue());
        });

        // 5. Cập nhật trạng thái từng Serial về AVAILABLE
        expiredItems.forEach(item -> {
            item.setStatus(InventoryItemStatus.AVAILABLE);
            item.setOrderId(null);
            item.setOrderNumber(null);
            item.setReservedAt(null);
        });

        itemRepo.saveAll(expiredItems);


        log.info("Dọn dẹp kho hoàn tất.");
    }
}
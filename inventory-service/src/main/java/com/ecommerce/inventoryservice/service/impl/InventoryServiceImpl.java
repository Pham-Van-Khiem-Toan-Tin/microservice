package com.ecommerce.inventoryservice.service.impl;

import com.ecommerce.inventoryservice.constants.Constants;
import com.ecommerce.inventoryservice.dto.exception.BusinessException;
import com.ecommerce.inventoryservice.entity.InventoryEntity;
import com.ecommerce.inventoryservice.entity.InventoryHistoryEntity;
import com.ecommerce.inventoryservice.entity.InventoryType;
import com.ecommerce.inventoryservice.repository.InventoryHistoryRepository;
import com.ecommerce.inventoryservice.repository.InventoryRepository;
import com.ecommerce.inventoryservice.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ecommerce.inventoryservice.constants.Constants.*;

@Service
public class InventoryServiceImpl implements InventoryService {
    @Autowired
    InventoryHistoryRepository historyRepo;
    @Autowired
    InventoryRepository inventoryRepo;

    @Override
    public void createInventory(String skuCode, Integer initialStock) {
        if (inventoryRepo.existsBySkuCode(skuCode)) {
            return; // Đã tồn tại thì bỏ qua hoặc cộng dồn (tùy logic)
        }

        InventoryEntity inv = new InventoryEntity();
        inv.setSkuCode(skuCode);
        inv.setTotalStock(initialStock);
        inv.setReservedStock(0);
        inv.setMinStockLevel(5);

        inventoryRepo.save(inv);

        // Log history
        logHistory(skuCode, initialStock, initialStock, InventoryType.IMPORT, "INITIAL_SETUP");
    }

    // 2. Giữ hàng (Khi khách bấm đặt hàng)
    @Transactional
    @Override
    public boolean reserveStock(String skuCode, Integer quantity) {
        // Query trả về số dòng bị ảnh hưởng (1 là thành công, 0 là thất bại)
        int updatedRows = inventoryRepo.reserveStock(skuCode, quantity);

        if (updatedRows > 0) {
            // Log history (Optional: có thể log hoặc không tùy mức độ chi tiết bạn muốn)
            return true;
        }
        return false; // Hết hàng
    }

    // 3. Thanh toán thành công -> Trừ kho thật
    @Transactional
    public void confirmOrder(String skuCode, Integer quantity, String orderId) {
        inventoryRepo.confirmStock(skuCode, quantity);

        // Lấy lại tồn kho hiện tại để lưu log cho đúng
        // (Lưu ý: query này chỉ để lấy số liệu ghi log, không ảnh hưởng logic trừ kho)
        InventoryEntity current = inventoryRepo.findBySkuCode(skuCode).orElseThrow(
                () -> new BusinessException(VALIDATE_FAIL)
        );
        logHistory(skuCode, -quantity, current.getTotalStock(), InventoryType.SOLD, orderId);
    }

    // ... Các hàm cancel, import stock khác
    @Override
    public void logHistory(String sku, int change, int stockAfter, InventoryType type, String ref) {
        InventoryHistoryEntity history = new InventoryHistoryEntity();
        history.setSkuCode(sku);
        history.setQuantityChange(change);
        history.setStockAfter(stockAfter);
        history.setType(type);
        history.setReferenceId(ref);
        historyRepo.save(history);
    }
}

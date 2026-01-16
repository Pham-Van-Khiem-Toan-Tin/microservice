package com.ecommerce.inventoryservice.service.impl;

import com.ecommerce.inventoryservice.constants.Constants;
import com.ecommerce.inventoryservice.dto.exception.BusinessException;
import com.ecommerce.inventoryservice.dto.request.InventoryAdjustRequest;
import com.ecommerce.inventoryservice.dto.response.InventoryAdjustResponse;
import com.ecommerce.inventoryservice.dto.response.InventoryDTO;
import com.ecommerce.inventoryservice.dto.response.InventoryStatus;
import com.ecommerce.inventoryservice.entity.InventoryEntity;
import com.ecommerce.inventoryservice.entity.InventoryHistoryEntity;
import com.ecommerce.inventoryservice.entity.InventoryType;
import com.ecommerce.inventoryservice.repository.InventoryHistoryRepository;
import com.ecommerce.inventoryservice.repository.InventoryRepository;
import com.ecommerce.inventoryservice.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static com.ecommerce.inventoryservice.constants.Constants.*;

@Service
public class InventoryServiceImpl implements InventoryService {
    @Autowired
    InventoryHistoryRepository historyRepo;
    @Autowired
    InventoryRepository inventoryRepo;

    @Override
    public Page<InventoryDTO> search(String keyword, List<String> fields, String sort, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.max(size, 1),
                parseSort(sort)
        );

        String kw = keyword == null ? "" : keyword.trim();
        Page<InventoryEntity> pageEntity =
                kw.isBlank()
                        ? inventoryRepo.findAll(pageable)
                        : inventoryRepo.findBySkuCodeContainingIgnoreCase(kw, pageable);

        return pageEntity.map(p -> {
            int total = p.getTotalStock();
            int reserved = p.getReservedStock();
            int available = total - reserved;

            int min = p.getMinStockLevel();
            boolean lowStock = available > 0 && available <= min;
            InventoryStatus status;
            if (available <= 0) {
                status = InventoryStatus.OUT_OF_STOCK;
            } else if (lowStock) {
                status = InventoryStatus.LOW_STOCK;
            } else {
                status = InventoryStatus.IN_STOCK;
            }
            return InventoryDTO.builder()
                    .id(p.getId().toString())
                    .skuCode(p.getSkuCode())
                    .totalStock(total)
                    .reservedStock(reserved)
                    .availableStock(available)
                    .lowStock(lowStock)
                    .minStockLevel(min)
                    .status(status)
                    .build();
        });
    }

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

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "skuCode",
            "totalStock",
            "reservedStock",
            "minStockLevel"
    );

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "skuCode");
        }

        String[] parts = sort.split(":");
        String field = parts[0].trim();
        String dir = parts.length > 1 ? parts[1].trim() : "asc";

        if (!ALLOWED_SORT_FIELDS.contains(field)) {
            field = "skuCode"; // fallback an toàn
        }

        Sort.Direction direction =
                "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;

        return Sort.by(direction, field);
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

    @Transactional
    @Override
    public InventoryAdjustResponse adjustInventory(InventoryAdjustRequest req) {
        String sku = req.getSkuCode() == null ? "" : req.getSkuCode().trim();
        Integer qtyObj = req.getQuantity();
        InventoryType type = req.getType();
        String ref = (req.getNote() == null || req.getNote().isBlank())
                ? "MANUAL_ADJUST"
                : req.getNote().trim();
        if (sku.isBlank() || type == null || qtyObj == null || qtyObj <= 0)
            throw new BusinessException(VALIDATE_FAIL);


        int qty = qtyObj;

        // snapshot before
        InventoryEntity before = inventoryRepo.findBySkuCode(sku)
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));

        int beforeTotal = safe(before.getTotalStock());
        int beforeReserved = safe(before.getReservedStock());

        int updated;
        int change; // quantityChange log history

        switch (type) {
            case IMPORT -> {
                updated = inventoryRepo.incTotal(sku, qty);
                change = qty;
            }
            case EXPORT -> {
                updated = inventoryRepo.decTotalIfAvailable(sku, qty);
                if (updated == 0) {
                    throw new BusinessException(VALIDATE_FAIL);
                }
                change = -qty;
            }
            case ADJUST -> {
                updated = inventoryRepo.setTotalIfNotBelowReserved(sku, qty);
                if (updated == 0) {
                    throw new BusinessException(VALIDATE_FAIL);
                }
                change = qty - beforeTotal; // delta
            }
            default -> throw new BusinessException(VALIDATE_FAIL);
        }

        if (updated == 0) {
            // trường hợp rất hiếm: sku không tồn tại trong câu UPDATE
            throw new BusinessException(VALIDATE_FAIL);
        }

        // snapshot after (1 lần thôi)
        InventoryEntity after = inventoryRepo.findBySkuCode(sku)
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));

        int afterTotal = safe(after.getTotalStock());
        int afterReserved = safe(after.getReservedStock());

        // log history: quantityChange = change, stockAfter = afterTotal
        logHistory(sku, change, afterTotal, type, ref);

        return InventoryAdjustResponse.builder()
                .skuCode(sku)
                .totalStock(afterTotal)
                .reservedStock(afterReserved)
                .availableStock(afterTotal - afterReserved)
                .build();
    }

    private int safe(Integer v) {
        return v == null ? 0 : v;
    }
}

package com.ecommerce.catalogservice.dto.response.sku;

import com.ecommerce.catalogservice.entity.ImageEntity;
import com.ecommerce.catalogservice.entity.SkuSelect;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class SkuDetailDTO {
    private String id;

    private String spuId; // Link ngược về cha (Product)

    private String skuCode; // Mã kho duy nhất (VD: IP15-PRO-BLK-256)

    private String name; // Tên đầy đủ (VD: iPhone 15 Pro Max - Đen - 256GB)

    // --- GIÁ BÁN (Transaction Data) ---
    private Double price;         // Giá bán thực tế
    private Double originalPrice; // Giá niêm yết (để gạch đi)
    private Double costPrice;
    // --- ĐỊNH DANH BIẾN THỂ (Mapping) ---

    // Cách 2: Attribute List (Dùng cho Query/Filter chính xác)
    // Lưu cụ thể giá trị
    private List<SkuSelect> selections;

    // --- MEDIA RIÊNG ---
    private ImageEntity thumbnail; // Ảnh riêng cho SKU này (VD: Ảnh máy màu đen)

    // --- TRẠNG THÁI ---
    // Lưu ý: Tồn kho THẬT nằm ở Inventory Service.
    // Field này chỉ cache để hiển thị nhanh "Còn hàng" hay không.
    private Boolean active;

    private Boolean discontinued;
    private String discontinuedReason;

    private Integer stock;

    private Long soldCount; // Số lượng đã bán (Cache để sort)
}

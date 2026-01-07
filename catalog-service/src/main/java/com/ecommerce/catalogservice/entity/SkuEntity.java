package com.ecommerce.catalogservice.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "skus")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(name = "spu_price_idx", def = "{'spuId': 1, 'price': 1}")
public class SkuEntity {
    @Id
    private String id;

    @Indexed
    private String spuId; // Link ngược về cha (Product)

    @Indexed(unique = true)
    private String skuCode; // Mã kho duy nhất (VD: IP15-PRO-BLK-256)

    private String name; // Tên đầy đủ (VD: iPhone 15 Pro Max - Đen - 256GB)

    // --- GIÁ BÁN (Transaction Data) ---
    @Indexed
    private Double price;         // Giá bán thực tế
    private Double originalPrice; // Giá niêm yết (để gạch đi)

    // --- ĐỊNH DANH BIẾN THỂ (Mapping) ---

    // Cách 2: Attribute List (Dùng cho Query/Filter chính xác)
    // Lưu cụ thể giá trị
    private List<SkuSpecs> specs;

    // --- MEDIA RIÊNG ---
    private ImageEntity image; // Ảnh riêng cho SKU này (VD: Ảnh máy màu đen)

    // --- TRẠNG THÁI ---
    // Lưu ý: Tồn kho THẬT nằm ở Inventory Service.
    // Field này chỉ cache để hiển thị nhanh "Còn hàng" hay không.
    private Boolean isAvailable;
    private Integer stock;

    private Long soldCount; // Số lượng đã bán (Cache để sort)
    private String updatedBy;
    private Instant createdAt;
    private Instant updatedAt;

}

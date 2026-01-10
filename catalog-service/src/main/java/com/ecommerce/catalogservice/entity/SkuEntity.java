package com.ecommerce.catalogservice.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;

@Document(collection = "skus")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
        @CompoundIndex(name = "spu_price_idx", def = "{'spu_id': 1, 'price': 1}"),
        @CompoundIndex(name = "spu_selections", def = "{'spu_id': 1, 'selections.code': 1, 'valueId': 1}")
})
public class SkuEntity {
    @Id
    private String id;

    @Indexed
    @Field("spu_id")
    private String spuId; // Link ngược về cha (Product)

    @Indexed(unique = true)
    @Field("sku_code")
    private String skuCode; // Mã kho duy nhất (VD: IP15-PRO-BLK-256)

    private String name; // Tên đầy đủ (VD: iPhone 15 Pro Max - Đen - 256GB)

    // --- GIÁ BÁN (Transaction Data) ---
    @Indexed
    private Double price;
    @Field("original_price")// Giá bán thực tế
    private Double originalPrice; // Giá niêm yết (để gạch đi)
    @Field("cost_price")// Giá bán thực tế
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
    @Indexed
    private Boolean active;

    private Boolean discontinued;
    @Field("discontinued_reason")
    private String discontinuedReason;

    @Field("has_orders")
    private Boolean hasOrders;
    @Field("inventory_pushed")
    private Boolean inventoryPushed;
    private Integer stock;
    @Field("sold_count")
    private Long soldCount; // Số lượng đã bán (Cache để sort)
    private String updatedBy;
    private Instant createdAt;
    private Instant updatedAt;

}

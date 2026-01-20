package com.ecommerce.catalogservice.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
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
    private BigDecimal price;
    @Field("original_price")// Giá bán thực tế
    private BigDecimal originalPrice; // Giá niêm yết (để gạch đi)
    @Field("cost_price")// Giá bán thực tế
    private BigDecimal costPrice;
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
    private SkuStatus active;

    private DiscontinuedType discontinued;
    @Field("discontinued_reason")
    private String discontinuedReason;

    private Integer stock;
    private String updatedBy;
    private Instant createdAt;
    private Instant updatedAt;

}

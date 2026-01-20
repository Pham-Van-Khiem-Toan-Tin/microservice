package com.ecommerce.catalogservice.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document("reviews")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductReviewEntity {
    @Id
    private String id;

    @Indexed // Index để tìm nhanh các review của 1 sản phẩm
    private String productId;
    private String skuId;
    private String userId;
    private String orderId;
    @Indexed(unique = true)
    private String orderItemId;

    private Integer rating; // 1-5 sao
    private String comment;
    private String userName;
    private String userAvatar;
    // Snapshot thông tin SKU tại thời điểm mua
    private String skuAttributes;

    // Danh sách ảnh hoặc video

    private ReviewStatus status; // PENDING, APPROVED, HIDDEN
    private Boolean isPurchased;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}



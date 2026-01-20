package com.ecommerce.identityservice.dto.response;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductListItemResponse {
    private String productId;
    private String name;
    private String slug;
    private Integer numberOfReviews;
    private Double averageRating;

    // Xử lý giá tiền (Dùng Double để an toàn với số khoa học như 2.4E7)
    private Double minPrice;
    private Double maxPrice;

    private ImageRef thumbnail;
    private BrandRef brand;
    private CategoryRef category;

    private List<VariantGroup> variantGroups;
    private List<ProductSku> skus;

    // --- Inner Classes (Hoặc tách file riêng tùy bạn) ---

    @Data
    public static class ImageRef {
        private String url;
    }

    @Data
    public static class BrandRef {
        private String id;
        private String name;
        private String slug;
    }

    @Data
    public static class CategoryRef {
        private String id;
        private String name;
        private String slug;
    }

    @Data
    public static class VariantGroup {
        private String id;
        private String label;
        private List<VariantValue> values;
    }

    @Data
    public static class VariantValue {
        private String id;     // vd: "red"
        private String value;  // vd: "Đỏ"
        // Thêm field khác nếu ES trả về nhiều hơn
    }

    @Data
    public static class ProductSku {
        private String skuId;
        private String skuCode;
        private String name;
        private Double price;
        private Double originalPrice;
        private ImageRef thumbnail; // Tái sử dụng class ImageRef
        private List<SkuSelection> selections;
        private Boolean active;
        private String status;
    }

    @Data
    public static class SkuSelection {
        private String groupId; // map với VariantGroup.id
        private String valueId; // map với VariantValue.id
    }
}

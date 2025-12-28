package com.ecommerce.catalogservice.entity;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Document(collection = "product_variants")
@Getter
@Setter
public class ProductVariantEntity {
    @Id
    private ObjectId id;

    @Indexed
    private ObjectId productId;

    @Indexed(unique = true)
    private String sku;

    private BigDecimal price;
    private Integer inStock;

    // Variant attributes (RAM, COLOR, STORAGE)
    private Map<String, String> attributes;

    private List<Image> variantImages;
    private String status;

    private Instant createdAt;

    @Getter
    @Setter
    public static class Image {
        private String link;
        private String alt;
        private Boolean isPrimary;
    }
}

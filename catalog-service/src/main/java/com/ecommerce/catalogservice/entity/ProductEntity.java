package com.ecommerce.catalogservice.entity;


import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document("products")
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Data
public class ProductEntity {
    @Id
    private ObjectId id;

    private String name;

    @Indexed(unique = true)
    private String slug;

    private ObjectId categoryId;        // leaf category
    private ObjectId attributeSetId;

    // Product-level attributes
    private Map<String, String> productAttributes;

    private Image mainImage;
    private String description;
    private String status;

    private Instant createdAt;
    private Instant updatedAt;

    @Getter
    @Setter
    public static class Image {
        private String link;
        private String alt;
        private String publicId;
    }

}

package com.ecommerce.catalogservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@AllArgsConstructor
public class ImageEntity {
    @Field("image_url")
    private String imageUrl;
    @Field("image_public_id")
    private String imagePublicId;
}

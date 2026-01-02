package com.ecommerce.catalogservice.entity;


import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Document(collection = "brands")
@Getter
@Setter
public class BrandEntity {
    @Id
    private String id;
    @Indexed(unique = true)
    private String code;
    private String name;

    private String slug;
    @Field("image_url")
    private String imageUrl;
    @Field("public_id_image")
    private String publicIdImage;
    private BrandStatus status;

    private String updatedBy;
    private Instant updatedAt;
    private Instant createdAt;
}

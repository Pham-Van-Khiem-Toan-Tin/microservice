package com.ecommerce.catalogservice.entity;


import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "brands")
@Getter
@Setter
public class BrandEntity {
    @Id
    private ObjectId id;

    private String name;

    @Indexed(unique = true)
    private String slug;
}

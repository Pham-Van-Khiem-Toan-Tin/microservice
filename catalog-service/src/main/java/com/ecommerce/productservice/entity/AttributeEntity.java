package com.ecommerce.productservice.entity;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collation = "attributes")
@Getter
@Setter
public class AttributeEntity {
    @Id
    private ObjectId id;

    @Indexed(unique = true)
    private String code;

    private String name;
    private String dataType;
    private String unit;
    private List<String> options;

    private Instant createdAt;
}

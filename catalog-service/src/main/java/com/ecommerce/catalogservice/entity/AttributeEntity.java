package com.ecommerce.catalogservice.entity;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "attributes")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
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

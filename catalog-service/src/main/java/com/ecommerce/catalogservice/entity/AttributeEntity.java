package com.ecommerce.catalogservice.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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
    private String id;

    @Indexed(unique = true)
    private String code;

    private String label;
    @Field("type")
    private AttributeDataType dataType;
    private String unit;
    private List<OptionEntity> options;

    private String updatedBy;
    private Instant updatedAt;
    private Instant createdAt;
}

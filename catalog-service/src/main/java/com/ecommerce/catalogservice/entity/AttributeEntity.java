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
    @Indexed
    private String label;
    @Field("type")
    private AttributeDataType dataType;
    private String unit;
    private List<OptionEntity> options;
    @Builder.Default
    private Boolean active = true;
    @Builder.Default
    private Boolean deleted = false;
    private String updatedBy;
    private Instant updatedAt;
    private Instant createdAt;
}

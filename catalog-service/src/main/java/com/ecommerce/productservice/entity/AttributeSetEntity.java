package com.ecommerce.productservice.entity;


import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "attribute_sets")
@Getter
@Setter
public class AttributeSetEntity {
    @Id
    private ObjectId id;

    @Indexed(unique = true)
    private String code;

    private String name;
    private String slug;

    private List<AttributeRule> attributes;

    private Instant createdAt;
    @Getter
    @Setter
    public static class AttributeRule {
        private String attributeCode;
        private Boolean usedForVariant;
        private Boolean required;
    }
}

package com.ecommerce.catalogservice.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class SkuSpecs {
    private String k;
    private String v;
    @Field("val_id")
    private String valId;
}

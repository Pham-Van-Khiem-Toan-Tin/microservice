package com.ecommerce.catalogservice.dto.request.product;

import lombok.Data;

@Data
public class SkuOptionForm {
    private String id;
    private String groupId;
    private String value;
}

package com.ecommerce.catalogservice.dto.request.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkuOptionForm {
    private String id;
    private String groupId;
}

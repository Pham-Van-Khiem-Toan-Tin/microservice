package com.ecommerce.catalogservice.dto.request;

import lombok.Data;

import java.util.Set;

@Data
public class AttributeOptionForm {
    private Set<String> attributeIds;
    private String keyword;
}

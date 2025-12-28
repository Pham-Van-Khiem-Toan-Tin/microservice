package com.ecommerce.catalogservice.dto.request;

import lombok.Data;

@Data
public class CategoryForm {
    private String id;
    private String name;
    private String parent;
}

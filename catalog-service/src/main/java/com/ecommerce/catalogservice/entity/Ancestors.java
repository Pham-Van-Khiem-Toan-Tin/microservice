package com.ecommerce.catalogservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Ancestors {
    private String id;
    private String name;
    private String slug;
}

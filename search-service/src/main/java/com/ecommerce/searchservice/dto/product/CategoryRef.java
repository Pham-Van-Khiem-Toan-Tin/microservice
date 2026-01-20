package com.ecommerce.searchservice.dto.product;

import lombok.Data;

import java.util.List;

@Data
public class CategoryRef {
    private String id;
    private String name;
    private String slug;
    private List<String> ancestorIds;
    private String thumbnail;
}
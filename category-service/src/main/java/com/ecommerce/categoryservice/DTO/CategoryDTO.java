package com.ecommerce.categoryservice.DTO;

import lombok.Data;

import java.util.List;

@Data
public class CategoryDTO {
    private Long id;
    private String name;
    private List<CategoryDTO> children; // Chỉ chứa danh sách children

    public CategoryDTO(Long id, String name, List<CategoryDTO> children) {
        this.id = id;
        this.name = name;
        this.children = children;
    }
}

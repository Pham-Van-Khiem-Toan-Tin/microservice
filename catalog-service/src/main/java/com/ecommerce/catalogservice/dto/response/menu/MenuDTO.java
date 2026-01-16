package com.ecommerce.catalogservice.dto.response.menu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MenuDTO {
    private String id;
    private String name;
    private String slug;
    private String parentId;
    private String icon;
    @Builder.Default
    private List<MenuDTO> children = new ArrayList<>();
    @Builder.Default
    private List<BrandDTO> brands = new ArrayList<>();
}

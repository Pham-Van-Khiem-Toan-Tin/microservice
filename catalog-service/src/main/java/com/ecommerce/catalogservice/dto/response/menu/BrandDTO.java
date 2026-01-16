package com.ecommerce.catalogservice.dto.response.menu;

import com.ecommerce.catalogservice.entity.ImageEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandDTO {
    private String id;
    private String name;
    private String slug;
    private ImageEntity logo;
}

package com.ecommerce.catalogservice.dto.response.product;

import com.ecommerce.catalogservice.entity.ImageEntity;
import com.ecommerce.catalogservice.entity.SkuSelect;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SkuDTO {
    private String id;
    private String name;
    private String skuCode;
    private Double price;
    private Double originalPrice;
    private List<SkuSelect> selections;
    private ImageEntity thumbnail;
}

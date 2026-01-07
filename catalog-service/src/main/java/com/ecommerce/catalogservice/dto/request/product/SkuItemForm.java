package com.ecommerce.catalogservice.dto.request.product;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class SkuItemForm {
    private String code;
    private String name;
    private Double price;
    private Double costPrice;
    private Double originalPrice;
    private Integer stock;
    private MultipartFile image;
    private List<SkuOptionForm> specs;

}

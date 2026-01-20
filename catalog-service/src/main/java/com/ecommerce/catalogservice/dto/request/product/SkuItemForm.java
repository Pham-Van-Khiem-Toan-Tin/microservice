package com.ecommerce.catalogservice.dto.request.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkuItemForm {
    private String id;
    private String code;
    private String name;
    private BigDecimal price;
    private BigDecimal costPrice;
    private BigDecimal originalPrice;
    private Integer stock;
    private Boolean active;
    private MultipartFile image;
    private List<SkuOptionForm> specs;

}

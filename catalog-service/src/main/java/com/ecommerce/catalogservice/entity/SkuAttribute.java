package com.ecommerce.catalogservice.entity;

import lombok.Data;

@Data
public class SkuAttribute {
    private String id;    // (Optional) ID giá trị
    private String label; // VD: Đỏ
    private String value;
}

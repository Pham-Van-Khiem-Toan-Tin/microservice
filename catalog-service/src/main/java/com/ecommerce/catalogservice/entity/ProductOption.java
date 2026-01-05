package com.ecommerce.catalogservice.entity;


import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOption {
    private String id;   // Tự sinh hoặc ID attribute (VD: opt_color)
    private String label; // Tên nhóm (VD: Màu sắc)
    private List<KV> values;
}

package com.ecommerce.catalogservice.entity;


import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Variant {
    private String sku;        // unique within product
    private String variantKey; // attr_color=Black|attr_ram=16GB

    private Long price;
    private Long stock;
    private String image;

    private List<KV> options;
}

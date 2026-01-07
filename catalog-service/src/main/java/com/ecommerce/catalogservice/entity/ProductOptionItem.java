package com.ecommerce.catalogservice.entity;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOptionItem {
    private String id;
    private String value;
    private Boolean active;
    private Boolean deprecated;
}

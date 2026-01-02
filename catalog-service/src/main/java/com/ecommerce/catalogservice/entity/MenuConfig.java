package com.ecommerce.catalogservice.entity;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuConfig {
    private boolean enabled;
    private int order;
    private String icon;
    private String bannerUrl;
    private String highlightText;
}

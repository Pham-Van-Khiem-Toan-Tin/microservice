package com.ecommerce.catalogservice.dto.request;

import lombok.Data;

@Data
public class MenuForm {
    private boolean enabled;
    private Integer displayOrder;
    private String icon;
}

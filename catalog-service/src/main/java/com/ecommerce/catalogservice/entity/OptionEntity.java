package com.ecommerce.catalogservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
@AllArgsConstructor
public class OptionEntity {
    private String id;
    private String label;
    private Boolean active;
    private Boolean deprecated;
    private Integer displayOrder;
}

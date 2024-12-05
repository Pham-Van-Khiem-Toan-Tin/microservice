package com.ecommerce.identityservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProvinceDTO {
    private long id;
    private String name;
}

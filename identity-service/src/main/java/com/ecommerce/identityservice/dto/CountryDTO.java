package com.ecommerce.identityservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CountryDTO {
    private String id;
    private String name;
}

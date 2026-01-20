package com.ecommerce.identityservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressLocationDto {
    private String code;
    private String name;
}

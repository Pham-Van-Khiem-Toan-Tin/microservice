package com.ecommerce.catalogservice.dto.response;

import com.ecommerce.catalogservice.entity.BrandStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BrandDTOS {
    private String id;
    private String name;
    private List<String> categories ;
    private BrandStatus status;
}

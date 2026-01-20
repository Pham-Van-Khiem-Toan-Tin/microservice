package com.ecommerce.catalogservice.dto.response.category;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FilterDTO {
    private String id;
    private List<FilterSpecDTO> filters;
}

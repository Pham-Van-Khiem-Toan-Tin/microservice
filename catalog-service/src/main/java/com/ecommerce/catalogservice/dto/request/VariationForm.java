package com.ecommerce.catalogservice.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class VariationForm {
    private String name;
    private List<String> option;
}

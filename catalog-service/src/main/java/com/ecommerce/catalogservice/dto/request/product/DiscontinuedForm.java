package com.ecommerce.catalogservice.dto.request.product;

import lombok.Data;

@Data
public class DiscontinuedForm {
    private Boolean discontinued;
    private String reason;
}

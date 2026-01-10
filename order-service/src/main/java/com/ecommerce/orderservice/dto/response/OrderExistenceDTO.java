package com.ecommerce.orderservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderExistenceDTO {
    private Boolean exists;
}

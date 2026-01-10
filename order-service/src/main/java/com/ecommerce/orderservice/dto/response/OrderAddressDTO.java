package com.ecommerce.orderservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class OrderAddressDTO {
    private UUID id;
    private String contactName;
    private String phone;
    private String addressDetail;
    private String city;
    private String district;
    private String ward;
}

package com.ecommerce.orderservice.dto.response;

import lombok.Data;

@Data
public class AddressDTO {
    private String receiverName;
    private String phone;
    private String provinceName;
    private String districtName;
    private String wardName;
    private String detailAddress;
}

package com.ecommerce.identityservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressDTO {
    private String receiverName;
    private String phone;
    private String provinceName;
    private String districtName;
    private String wardName;
    private String detailAddress;
}

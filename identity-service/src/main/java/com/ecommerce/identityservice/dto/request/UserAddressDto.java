package com.ecommerce.identityservice.dto.request;

import com.ecommerce.identityservice.entity.AddressType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserAddressDto {
    private String id;
    private String receiverName;
    private String phone;
    private String provinceCode;
    private String provinceName;
    private String districtCode;
    private String districtName;
    private String wardCode;
    private String wardName;
    private String detailAddress;
    private Boolean isDefault;
    private AddressType type;
}

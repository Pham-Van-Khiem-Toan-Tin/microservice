package com.ecommerce.identityservice.dto.response;

import com.ecommerce.identityservice.entity.AddressType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserAddressRequestDto {
    private String receiverName;
    private String phone;
    private String provinceCode;
    private String districtCode;
    private String wardCode;
    private String detailAddress;
    private Boolean isDefault;
    private AddressType type;
}

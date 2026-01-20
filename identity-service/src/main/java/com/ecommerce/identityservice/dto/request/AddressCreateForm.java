package com.ecommerce.identityservice.dto.request;

import com.ecommerce.identityservice.entity.AddressType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressCreateForm {
    private String receiverName;
    private String phone;
    private String provinceCode;
    private String districtCode;
    private String wardCode;
    private String detailAddress;
    private Boolean isDefault;
    private AddressType type; // HOME, WORK, ..
}

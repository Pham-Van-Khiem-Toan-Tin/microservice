package com.ecommerce.identityservice.dto.request;

import com.ecommerce.identityservice.entity.AddressType;
import lombok.Data;

@Data
public class AddressUpdateForm {
    private String id;
    private String receiverName;
    private String phone;
    private String detailAddress; // Số nhà, đường

    // Chỉ cần nhận Code, tên Tỉnh/Huyện/Xã Backend tự lấy hoặc Frontend gửi kèm (ở đây tôi giả sử FE gửi code, BE tự map hoặc FE gửi cả text nếu muốn đơn giản)
    // Để đơn giản và chính xác, ta lưu những gì Frontend gửi lên
    private String provinceCode;
    private String provinceName;

    private String districtCode;
    private String districtName;
    private AddressType type;

    private String wardCode;
    private String wardName;

    private Boolean isDefault;
}

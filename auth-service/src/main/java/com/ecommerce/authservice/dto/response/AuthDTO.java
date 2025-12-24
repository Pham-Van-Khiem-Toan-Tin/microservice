package com.ecommerce.authservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthDTO {
    private String userId;
    private String userName;

}

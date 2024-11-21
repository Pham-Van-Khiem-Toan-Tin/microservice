package com.ecommerce.identityservice.dto;

import lombok.Data;

@Data
public class RenewTokenDTO {
    private String sessionId;
    private String accessToken;
}

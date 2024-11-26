package com.ecommerce.identityservice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RenewTokenDTO {
    private String sessionId;
    private String accessToken;
    private long expireIn;
}

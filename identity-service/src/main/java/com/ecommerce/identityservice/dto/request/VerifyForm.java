package com.ecommerce.identityservice.dto.request;

import lombok.Data;

@Data
public class VerifyForm {
    private String email;
    private String otp;
}

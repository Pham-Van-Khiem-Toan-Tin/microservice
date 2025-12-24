package com.ecommerce.authservice.dto.response;

import com.ecommerce.authservice.constant.Constants;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final Constants responseCode;
    public BusinessException(Constants responseCode) {
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }
}

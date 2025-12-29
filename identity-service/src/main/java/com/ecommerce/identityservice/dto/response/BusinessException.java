package com.ecommerce.identityservice.dto.response;

import com.ecommerce.identityservice.constants.Constants;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final Constants responseCode;
    public BusinessException(Constants responseCode) {
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }
}

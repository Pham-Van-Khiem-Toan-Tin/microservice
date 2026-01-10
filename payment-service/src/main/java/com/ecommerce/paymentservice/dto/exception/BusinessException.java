package com.ecommerce.paymentservice.dto.exception;

import com.ecommerce.paymentservice.constants.Constants;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final Constants responseCode;
    public BusinessException(Constants responseCode) {
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }
}

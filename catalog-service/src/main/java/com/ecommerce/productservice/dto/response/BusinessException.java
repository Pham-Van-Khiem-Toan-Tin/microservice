package com.ecommerce.productservice.dto.response;

import com.ecommerce.productservice.constants.ResponseCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ResponseCode code;
    public BusinessException(ResponseCode code) {
        super(code.getMessage());
        this.code = code;
    }
}

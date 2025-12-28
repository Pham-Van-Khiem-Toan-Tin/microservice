package com.ecommerce.catalogservice.dto.response;

import com.ecommerce.catalogservice.constants.Constants;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final Constants responseCode;
    public BusinessException(Constants responseCode) {
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }
}

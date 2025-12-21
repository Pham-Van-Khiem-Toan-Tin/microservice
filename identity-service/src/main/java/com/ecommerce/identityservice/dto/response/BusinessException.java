package com.ecommerce.identityservice.dto.response;

import com.ecommerce.identityservice.constants.Constants;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final Constants constants;

    public BusinessException(Constants constants) {
        super(constants.getMessage());
        this.constants = constants;
    }
}

package com.ecommerce.identityservice.dto;

import com.ecommerce.identityservice.constants.Constants;
import lombok.Getter;

@Getter
public class CustomException extends Exception{
    private final int status;
    private final String message;
    public CustomException(int status, String message) {
        super(message);
        this.status = status;
        this.message = message;
    }
    public CustomException(Constants constants) {
        this.message = constants.getMessage();
        this.status = constants.getCode();
    }
}

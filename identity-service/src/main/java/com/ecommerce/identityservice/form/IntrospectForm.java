package com.ecommerce.identityservice.form;

import lombok.Data;

@Data
public class IntrospectForm {
    private String token;
    private String sessionId;
}

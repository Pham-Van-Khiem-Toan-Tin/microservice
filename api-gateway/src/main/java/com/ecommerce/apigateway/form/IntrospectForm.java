package com.ecommerce.apigateway.form;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IntrospectForm {
    private String token;
}

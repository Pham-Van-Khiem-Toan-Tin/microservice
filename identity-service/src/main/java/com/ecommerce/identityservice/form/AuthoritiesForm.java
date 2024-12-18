package com.ecommerce.identityservice.form;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthoritiesForm {
    private String clientId;
    private String id;
    private String name;
    private String description;
}

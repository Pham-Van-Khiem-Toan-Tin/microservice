package com.ecommerce.authservice.dto.request;

import lombok.Getter;

import java.util.Set;

@Getter
public class SubFunctionOptionForm {
    private String keyword;
    private Set<String> ids;
}

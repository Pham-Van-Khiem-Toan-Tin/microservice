package com.ecommerce.authservice.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubFunctionForm {
    private String id;
    private String code;
    private String name;
    private String description;
    private String functionId;
}

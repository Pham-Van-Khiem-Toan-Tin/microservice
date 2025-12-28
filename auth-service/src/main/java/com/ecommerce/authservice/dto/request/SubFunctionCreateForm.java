package com.ecommerce.authservice.dto.request;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubFunctionCreateForm {
    private String code;
    private String name;
    private String description;
    private String functionId;
}

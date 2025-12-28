package com.ecommerce.authservice.dto.request;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubFunctionEditForm {
    private String id;
    private String code;
    private String name;
    private String description;
    private String functionId;
}

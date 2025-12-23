package com.ecommerce.authservice.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FunctionDTO {
    private String id;
    private String name;
    private String description;
    private int sortOrder;
    private long quantityPermission;
}

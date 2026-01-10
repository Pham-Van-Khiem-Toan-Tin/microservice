package com.ecommerce.catalogservice.dto.response.attribute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeCountDTO {
    private String id;   // _id
    private long cnt;
}

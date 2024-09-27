package com.ecommerce.apigateway.dto.response;

import com.ecommerce.apigateway.entity.User;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntrospectResponse {
    private boolean valid;
    private User user;
}

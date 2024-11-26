package com.ecommerce.apigateway.service;

import com.ecommerce.apigateway.client.IdentityClient;
import com.ecommerce.apigateway.dto.IntrospectResponse;
import com.ecommerce.apigateway.form.IntrospectForm;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IntrospectService {
    IdentityClient identityClient;
    public Mono<IntrospectResponse> introspect(String token, String sessionId) {
        return identityClient.introspect(IntrospectForm.builder()
                .token(token)
                .sessionId(sessionId)
                .build());
    }
}

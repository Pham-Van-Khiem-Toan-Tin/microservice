package com.ecommerce.apigateway.service;

import com.ecommerce.apigateway.client.IdentityClient;
import com.ecommerce.apigateway.dto.IntrospectResponse;
import com.ecommerce.apigateway.form.IntrospectForm;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class IntrospectService {
    private IdentityClient identityClient;
    public Mono<IntrospectResponse> introspect(String token) {
        return identityClient.introspect(IntrospectForm.builder()
                .token(token)
                .build());
    }
}

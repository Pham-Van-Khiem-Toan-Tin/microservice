package com.ecommerce.apigateway.service;

import com.ecommerce.apigateway.dto.request.IntrospectRequest;
import com.ecommerce.apigateway.dto.response.ApiResponse;
import com.ecommerce.apigateway.dto.response.IntrospectResponse;
import com.ecommerce.apigateway.repository.IdentityClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class IdentityService {
    @Autowired
    private final IdentityClient identityClient;
    public Mono<ApiResponse<IntrospectResponse>> introspect(String token) {
        return identityClient.introspect(IntrospectRequest.builder().token(token).build());
    }
}

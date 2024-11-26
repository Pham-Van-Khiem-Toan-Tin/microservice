package com.ecommerce.apigateway.client;

import com.ecommerce.apigateway.dto.IntrospectResponse;
import com.ecommerce.apigateway.form.IntrospectForm;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

public interface IdentityClient {
    @PostExchange(url = "/auth/introspect", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<IntrospectResponse> introspect(@RequestBody IntrospectForm request);
}

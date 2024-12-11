package com.ecommerce.apigateway.client;

import com.ecommerce.apigateway.dto.IntrospectResponse;
import com.ecommerce.apigateway.form.IntrospectForm;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface IdentityClient {
    @PostExchange(url = "/oauth2/introspect", contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    Mono<IntrospectResponse> introspect(@RequestBody MultiValueMap<String, String> request);
}

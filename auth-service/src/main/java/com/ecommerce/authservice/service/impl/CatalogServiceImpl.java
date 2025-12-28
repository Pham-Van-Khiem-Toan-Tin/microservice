package com.ecommerce.authservice.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

//@Service
//public class CatalogServiceImpl implements CatalogService {
//    @Autowired
//    WebClient webClient;
//    @Value("${services.catalog.base-url}")
//    private String baseUrl;
//
//    @Override
//    public Mono<String> test() {
//
//        return webClient.get()
//                .uri(baseUrl + "/test")
//                .retrieve()
//                .bodyToMono(String.class);
//    }
//}

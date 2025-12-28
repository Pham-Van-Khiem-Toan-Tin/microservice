package com.ecommerce.authservice.service.impl;


import reactor.core.publisher.Mono;

public interface CatalogService {
    Mono<String> test();
}

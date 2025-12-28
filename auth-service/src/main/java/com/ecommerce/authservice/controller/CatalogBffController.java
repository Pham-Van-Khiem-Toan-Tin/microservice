package com.ecommerce.authservice.controller;

import com.ecommerce.authservice.service.impl.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

//@RestController
//@RequestMapping("/test")
//public class CatalogBffController {
//    @Autowired
//    CatalogService catalogService;
//    @GetMapping
//    @PreAuthorize("hasAuthority('VIEW_PROFILE')")
//    public Mono<String> test() {
//        return catalogService.test();
//    }
//}

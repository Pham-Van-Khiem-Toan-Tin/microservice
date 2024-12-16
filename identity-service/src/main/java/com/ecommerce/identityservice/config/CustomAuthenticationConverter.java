package com.ecommerce.identityservice.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class CustomAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        Map<String, List<String>> roleOfClients = (Map<String, List<String>>) source.getClaims().get("roles");
        String clientId = source.getClaimAsString("client_id");
        String userId = source.getSubject();
        List<String> roles = roleOfClients.get(clientId);
        List<GrantedAuthority> authoritiesList = new ArrayList<>();
        roles.stream().forEach(item -> {
            authoritiesList.add(new SimpleGrantedAuthority(item));
        });
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, authoritiesList);
        CustomAuthenticationDetail detail = CustomAuthenticationDetail.builder()
                .clientId(clientId)
                .token(source.getTokenValue())
                .build();
        authentication.setDetails(detail);
        return authentication;
    }

    @Override
    public <U> Converter<Jwt, U> andThen(Converter<? super AbstractAuthenticationToken, ? extends U> after) {
        return Converter.super.andThen(after);
    }
}

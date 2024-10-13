package com.ecommerce.categoryservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CustomAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    @Value("${jwt.client-id}")
    private String clientId;
    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        Map<String, Object> resourceAccess = (Map<String, Object>) source.getClaims().get("resource_access");
        Map<String, Object> clientResource = (Map<String, Object>) resourceAccess.get(clientId);
        List<String> roles = (List<String>) clientResource.get("roles");
        Collection<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(role))
                .collect(Collectors.toList());
        return new JwtAuthenticationToken(source, authorities);
    }
}

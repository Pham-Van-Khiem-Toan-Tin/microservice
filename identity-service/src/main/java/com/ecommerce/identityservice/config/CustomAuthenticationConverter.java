package com.ecommerce.identityservice.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CustomAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        List<String> functions = source.getClaimAsStringList("functions");
        List<String> subFunctions = source.getClaimAsStringList("subfunctions");
        String userId = source.getSubject();
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (functions != null) {
            for (String function : functions) {
                authorities.add(new SimpleGrantedAuthority(function));
            }
        }
        if (subFunctions != null) {
            for (String subFunction : subFunctions) {
                authorities.add(new SimpleGrantedAuthority(subFunction));
            }
        }
        return new JwtAuthenticationToken(source, authorities, userId);
    }
}

package com.ecommerce.bffadmin.config;

import com.ecommerce.bffadmin.utils.PemUtils;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

@Configuration
public class InternalJwtConfig {

    @Bean
    RSAKey rsaJwk() throws Exception {
        KeyPair kp = PemUtils.readKeyPair("keys/bff-private.pem", "keys/bff-public.pem");
        return new RSAKey.Builder((RSAPublicKey) kp.getPublic())
                .privateKey((RSAPrivateKey) kp.getPrivate())
                .keyID("bff-key-1")
                .build();
    }

    @Bean
    JWKSource<SecurityContext> jwkSource(RSAKey rsaJwk) {
        return new ImmutableJWKSet<>(new JWKSet(rsaJwk));
    }

    @Bean
    JwtEncoder jwtEncoder(JWKSource<com.nimbusds.jose.proc.SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    // Services sẽ gọi endpoint này để lấy public key verify
    @RestController
    static class JwksController {
        private final RSAKey rsaKey;
        JwksController(RSAKey rsaKey) { this.rsaKey = rsaKey; }

        @GetMapping("/.well-known/jwks.json")
        public Map<String, Object> jwks() {
            return new JWKSet(rsaKey.toPublicJWK()).toJSONObject();
        }
    }
}


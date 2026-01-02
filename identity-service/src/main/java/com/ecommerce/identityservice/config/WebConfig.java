package com.ecommerce.identityservice.config;

import com.ecommerce.identityservice.reppository.ClientRepository;
import com.ecommerce.identityservice.reppository.JpaRegisteredClientRepository;
import com.ecommerce.identityservice.service.impl.CustomUserDetailService;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.RememberMeAuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.client.RestTemplate;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebConfig {
    @Autowired
    private IdpLoginSuccessHandler idpLoginSuccessHandler;
    @Autowired
    private CustomUserDetailService userDetailsService;
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();
        authorizationServerConfigurer
                .oidc(oidc -> oidc
                        .userInfoEndpoint(Customizer.withDefaults()) // <--- 1. Bật endpoint này lên
                );
        RequestMatcher endpointsMatcher = authorizationServerConfigurer.getEndpointsMatcher();

        http
                // CHỈ chain này match các endpoint OAuth2/OIDC
                .securityMatcher(endpointsMatcher)

                // mọi request thuộc matcher này phải auth
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())

                // CSRF ignore cho token/introspect/revoke...
                .csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher))

                // APPLY đúng instance configurer này
                .with(authorizationServerConfigurer, authServer ->
                        authServer.oidc(Customizer.withDefaults())
                )

                .cors(Customizer.withDefaults())

                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                );

        return http.build();
    }
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers("/register", "/terms", "/privacy", "/login", "/verify-email", "forgot-password", "new-password", "/role/**","/favicon.ico",
                                "/css/**", "/js/**", "/images/**", "/fontawesome/**", "/error", "/webjars/**", "/favicon.ico", "/.well-known/appspecific/com.chrome.devtools.json")
                        .permitAll()
                        .anyRequest().authenticated())
                .cors(Customizer.withDefaults())
                .formLogin(login -> {
                    login.loginPage("/login")
                            .successHandler(idpLoginSuccessHandler);
                })
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );
//                .rememberMe(rm -> rm
//                        .rememberMeParameter("remember-me")
//                        .tokenValiditySeconds(60 * 60 * 24 * 30)
//                        .key("chuoi-bi-mat-nao-do")
//                        .rememberMeCookieName("RM_ID")
//                        .userDetailsService(userDetailsService)
//                );
        return http.build();
    }
//    @Bean
//    RememberMeAuthenticationFilter rememberMeFilter() {
//        RememberMeAuthenticationFilter rememberMeFilter = new RememberMeAuthenticationFilter();
//        rememberMeFilter.setRememberMeServices(rememberMeServices());
//        rememberMeFilter.setAuthenticationManager(theAuthenticationManager);
//        return rememberMeFilter;
//    }
//
//    @Bean
//    TokenBasedRememberMeServices rememberMeServices() {
//        TokenBasedRememberMeServices rememberMeServices = new TokenBasedRememberMeServices();
//        rememberMeServices.setUserDetailsService(myUserDetailsService);
//        rememberMeServices.setKey("springRocks");
//        return rememberMeServices;
//    }
//
//    @Bean
//    RememberMeAuthenticationProvider rememberMeAuthenticationProvider() {
//        RememberMeAuthenticationProvider rememberMeAuthenticationProvider = new RememberMeAuthenticationProvider();
//        rememberMeAuthenticationProvider.setKey("springRocks");
//        return rememberMeAuthenticationProvider;
//    }
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return context -> {
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                context.getClaims().claims((claims) -> {
                   Authentication principal = context.getPrincipal();
                   CustomUserDetail principalCustomUserDetail = (CustomUserDetail) principal.getPrincipal();
                   claims.put("uid", principalCustomUserDetail.getId().toString());
                   Set<String> authorities = principal.getAuthorities().stream()
                           .map(GrantedAuthority::getAuthority)
                           .collect(Collectors.toSet());
                    claims.put("authorities", authorities);
                });
            }
        };
    }
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");
        grantedAuthoritiesConverter.setAuthorityPrefix("");
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }
    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        }
        catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }
}
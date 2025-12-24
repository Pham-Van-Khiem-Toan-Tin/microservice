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
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

@Configuration
@EnableWebSecurity
public class WebConfig {
    @Autowired
    ClientRepository clientRepository;
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
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(Customizer.withDefaults())
                )
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
                        .requestMatchers("/register", "/terms", "/privacy", "/login", "/verify-email", "forgot-password", "new-password", "/role/**",
                                "/css/**", "/js/**", "/images/**", "/fontawesome/**", "/images/**", "/webjars/**", "/favicon.ico", "/.well-known/appspecific/com.chrome.devtools.json")
                        .permitAll()
                        .anyRequest().authenticated())
                .cors(Customizer.withDefaults())
                .formLogin(login -> {
                    login.loginPage("/login")
                            .successHandler(idpLoginSuccessHandler);
                })
                .rememberMe(rm -> rm
                        .rememberMeParameter("remember-me")
                        .tokenValiditySeconds(60 * 60 * 24 * 30)  // 30 ngày
                        .key("chuoi-bi-mat-nao-do")
                        .userDetailsService(userDetailsService)
                );
        return http.build();
    }
    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        // ==> QUAN TRỌNG: Đừng xóa password vội để còn dùng nó tạo RememberMe Cookie
        provider.setForcePrincipalAsString(false);

        return new ProviderManager(provider);
    }
    private Consumer<OAuth2AuthorizationCodeRequestAuthenticationContext> validateClientAccess() {
        return ctx -> {
            String clientId = ctx.getRegisteredClient().getClientId();
            Authentication principal = ctx.getAuthentication();
            Set<String> authorities = AuthorityUtils.authorityListToSet(principal.getAuthorities());
            if ("admin-client".equals(clientId)) {
                // Client Admin bắt buộc phải có ROLE_ADMIN
                if (!authorities.contains("SUPER_ADMIN")) {
                    throw new OAuth2AuthenticationException(new OAuth2Error("access_denied", "Bạn không phải Admin", null));
                }
            }

            else if ("user-client".equals(clientId)) {
                // Client Customer bắt buộc phải có ROLE_CUSTOMER
                if (!authorities.contains("ROLE_CUSTOMER")) {
                    throw new OAuth2AuthenticationException(new OAuth2Error("access_denied", "Tài khoản khách hàng không hợp lệ", null));
                }
            }
        };
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.addAllowedOrigin("*");
        config.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", config);
        return source;
    }
    @Bean
    public JpaRegisteredClientRepository jpaRegisteredClientRepository(PasswordEncoder passwordEncoder) {
        String clientId = "user-client";
        JpaRegisteredClientRepository jpaRegisteredClientRepository = new JpaRegisteredClientRepository(clientRepository);
        RegisteredClient existing = jpaRegisteredClientRepository.findByClientId(clientId);
        if (existing == null) {
            RegisteredClient oidcClient = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId(clientId)
                    .clientSecret(passwordEncoder().encode("secret"))
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .redirectUri("http://localhost:8082/auth/oauth2/code/user-idp")
                    .scope(OidcScopes.OPENID)
                    .scope(OidcScopes.PROFILE)
                    .tokenSettings(TokenSettings.builder()
                            .accessTokenTimeToLive(Duration.ofMinutes(15))
                            .refreshTokenTimeToLive(Duration.ofDays(15))
                            .reuseRefreshTokens(true)
                            .build())
                    .clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build())
                    .build();
            jpaRegisteredClientRepository.save(oidcClient);
        }
        return jpaRegisteredClientRepository;
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
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
        return (context) -> {
            // Áp dụng cho cả ID Token và Access Token
            if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue()) ||
                    OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {

                Authentication authentication = context.getPrincipal();
                Object principal = authentication.getPrincipal();

                // Kiểm tra xem principal có phải là UserDetails của bạn không
                if (principal instanceof CustomUserDetail userDetails) {

                    // Lấy UUID từ UserDetails
                    String userId = userDetails.getId().toString();

                    // --- QUAN TRỌNG: GHI ĐÈ sub ---
                    context.getClaims().claim("uid", userId);

                    // (Tùy chọn) Thêm username vào claim khác nếu cần hiển thị
                    context.getClaims().claim("username", userDetails.getUsername());
                }
            }
        };
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
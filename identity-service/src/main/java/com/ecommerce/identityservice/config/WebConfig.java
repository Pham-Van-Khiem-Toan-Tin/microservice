package com.ecommerce.identityservice.config;

import com.ecommerce.identityservice.repository.AuthorizationRepository;
import com.ecommerce.identityservice.repository.ClientRepository;
import com.ecommerce.identityservice.repository.JpaRegisteredClientRepository;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;



@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebConfig {
    private final String[] publicEndpoint = {
            "/auth/introspect",
            "/auth/login",
            "/auth/register",
            "/auth/token",
            "/actuator/health",
            "/test",
            "/admin/**"
    };
    @Autowired
    CustomUserDetailService customUserDetailService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private CustomAuthenticationProvider authenticationProvider;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private CustomAuthenticationConverter customAuthenticationConverter;
    @Autowired
    private CustomJwtDecoder customJwtDecoder;
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(Customizer.withDefaults());

        http
                .exceptionHandling((exceptions) -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        ));
        return http.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(publicEndpoint)
                        .permitAll()
                        .requestMatchers("/oauth2/token", "/oauth2/authorize")
                        .anonymous()
                        .anyRequest()
                        .authenticated())
                .formLogin(Customizer.withDefaults())
                .rememberMe(Customizer.withDefaults())
                .oauth2ResourceServer((resourceServer) -> resourceServer
                        .jwt((jwt) -> jwt.decoder(customJwtDecoder)
                                .jwtAuthenticationConverter(customAuthenticationConverter)));
        return http.build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        return new JpaRegisteredClientRepository(clientRepository);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().issuer("http://localhost:8085").build();
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }
}

package com.ecommerce.authservice.config;

import com.ecommerce.authservice.repository.UserRepository;
import com.ecommerce.authservice.security.HttpCookieOAuth2AuthorizationRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebConfig {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;
    @Autowired
    private CustomRoleHeaderFilter customRoleHeaderFilter;
    // Inject cái Handler vừa viết
    @Autowired
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .authorizeHttpRequests(auth -> auth
            .requestMatchers("/favicon.ico").permitAll()
                .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .addFilterAfter(customRoleHeaderFilter, BearerTokenAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorizationEndpoint ->
                                authorizationEndpoint
                                        .authorizationRequestRepository(cookieAuthorizationRequestRepository))
                        .userInfoEndpoint(userInfo ->
                                userInfo.userAuthoritiesMapper(userAuthoritiesMapper()))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            response.sendRedirect("http://localhost:5173/login?error=" + exception.getMessage());
                        }));
        return http.build();
    }
    private GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            // 1. Duyệt qua thông tin user trả về từ IDP
            authorities.forEach(authority -> {
                if (authority instanceof OidcUserAuthority oidcUserAuth) {
                    // Lấy email hoặc subject id từ IDP
                    String userId = oidcUserAuth.getAttributes().get("uid").toString();

                    // 2. QUERY DATABASE CỦA BFF
                    // Tìm user trong bảng phân quyền của BFF
                    var localUser = userRepository.findById(UUID.fromString(userId))
                            .orElseThrow(() -> new RuntimeException("User chưa được phân quyền hệ thống"));

                    // 3. Map Role từ DB ra Spring Security Authority
                    localUser.getRole().getSubFunctions().forEach(roleEntity -> {
                        mappedAuthorities.add(new SimpleGrantedAuthority(roleEntity.getId()));
                    });
                }
            });

            // Trả về danh sách quyền lấy từ DB (Spring sẽ lưu cái này vào Redis)
            return mappedAuthorities;
        };
    }
}

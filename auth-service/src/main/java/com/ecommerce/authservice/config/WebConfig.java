package com.ecommerce.authservice.config;

import com.ecommerce.authservice.repository.UserRepository;
//import com.ecommerce.authservice.security.HttpCookieOAuth2AuthorizationRequestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class WebConfig {
    @Autowired
    private UserRepository userRepository;

//    @Autowired
//    private CustomRoleHeaderFilter customRoleHeaderFilter;

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
                        .requestMatchers("/favicon.ico", "/roles", "/api/**", "/functions", "/subfunctions").permitAll()
                        .anyRequest().authenticated())
                .formLogin(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
//                .addFilterAfter(customRoleHeaderFilter, BearerTokenAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo ->
                                userInfo.userAuthoritiesMapper(userAuthoritiesMapper()))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            log.error(exception.getMessage(), exception);
                            response.sendRedirect("http://localhost:5173/login?error=" + exception.getMessage());
                        }))
                .oauth2Client(Customizer.withDefaults());
        return http.build();
    }
    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .authorizationCode()
                        .refreshToken()
                        .clientCredentials()
                        .build();

        DefaultOAuth2AuthorizedClientManager authorizedClientManager =
                new DefaultOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientRepository);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }
//    @Bean
//    WebClient webClient(OAuth2AuthorizedClientManager authorizedClientManager) {
//        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
//                new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
////        oauth2Client.setDefaultOAuth2AuthorizedClient(true);
//        return WebClient.builder()
//                .apply(oauth2Client.oauth2Configuration())
//                .filter(addAuthoritiesHeader())
//                .filter(retryOn401WithRefresh(authorizedClientManager))
//                .build();
//    }
//    @Bean
//    public ExchangeFilterFunction addAuthoritiesHeader() {
//        return (request, next) -> {
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//            if (auth == null) return next.exchange(request);
//
//            String authorities = auth.getAuthorities().stream()
//                    .map(GrantedAuthority::getAuthority)
//                    .distinct()
//                    .reduce((a,b) -> a + "," + b)
//                    .orElse("");
//
//            ClientRequest mutated = ClientRequest.from(request)
//                    .header("X-Authorities", authorities)
//                    // nếu cần:
//                    // .header("X-User", auth.getName())
//                    .build();
//
//            return next.exchange(mutated);
//        };
//    }
//    @Bean
//    public ExchangeFilterFunction retryOn401WithRefresh(
//            OAuth2AuthorizedClientManager authorizedClientManager
//    ) {
//        return (request, next) -> next.exchange(request).flatMap(resp -> {
//            if (resp.statusCode() != HttpStatus.UNAUTHORIZED) {
//                return Mono.just(resp);
//            }
//
//            // Chỉ retry nếu đang là oauth2Login (có refresh token)
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//            if (!(auth instanceof OAuth2AuthenticationToken oat)) {
//                return Mono.just(resp);
//            }
//
//            // IMPORTANT: consume/close response body trước khi retry để tránh leak
//            return resp.releaseBody()
//                    .then(Mono.defer(() -> {
//                        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
//                                .withClientRegistrationId(oat.getAuthorizedClientRegistrationId())
//                                .principal(oat)
//                                .build();
//
//                        OAuth2AuthorizedClient refreshed = authorizedClientManager.authorize(authorizeRequest);
//                        if (refreshed == null || refreshed.getAccessToken() == null) {
//                            return next.exchange(request); // hoặc return 401 luôn
//                        }
//
//                        ClientRequest retryReq = ClientRequest.from(request)
//                                .headers(h -> h.set(HttpHeaders.AUTHORIZATION,
//                                        "Bearer " + refreshed.getAccessToken().getTokenValue()))
//                                .build();
//
//                        return next.exchange(retryReq);
//                    }));
//        });
//    }
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
                    localUser.getRole().getSubFunctions().forEach(ath -> {
                        mappedAuthorities.add(new SimpleGrantedAuthority(ath.getCode()));
                    });
                }
            });

            // Trả về danh sách quyền lấy từ DB (Spring sẽ lưu cái này vào Redis)
            return mappedAuthorities;
        };
    }
}

package com.ecommerce.authservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;


@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private static final String REG_ADMIN_IDP = "admin-idp";
    private static final String REG_USER_IDP = "user-idp";


    // ===== Frontend URLs =====
    private static final String ADMIN_FE_URL = "http://localhost:5173/";
    private static final String CUSTOMER_FE_URL = "http://localhost:5175/";
    private static final String NO_ADMIN_ACCESS_URL = "http://localhost:5173/?reason=NO_ADMIN_ACCESS";

    // ===== Session keys =====
    private static final String SESSION_ACCESS_TOKEN = "BFF_ACCESS_TOKEN";
    private static final String SESSION_AUTHORITIES = "AUTHORITIES";
    // 1. Inject Repository để lấy token
    @Autowired
    private OAuth2AuthorizedClientRepository authorizedClientRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            throw new IllegalStateException(
                    "Expected OAuth2AuthenticationToken but got: "
                            + (authentication == null ? "null" : authentication.getClass().getName())
            );
        }
        HttpSession session = request.getSession(true);
        OAuth2AuthorizedClient authorizedClient = authorizedClientRepository.loadAuthorizedClient(
                oauthToken.getAuthorizedClientRegistrationId(),
                authentication,
                request
        );
        if (authorizedClient != null) {
            String accessToken = authorizedClient.getAccessToken().getTokenValue();
            Set<String> authorities = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
            String rolesJson = new ObjectMapper().writeValueAsString(authorities);
            session.setAttribute(SESSION_AUTHORITIES, rolesJson);
            session.setAttribute(SESSION_ACCESS_TOKEN, accessToken);
        }
        String regId = oauthToken.getAuthorizedClientRegistrationId();

        String targetUrl;

        if (REG_ADMIN_IDP.equals(regId)) {
            targetUrl =  ADMIN_FE_URL;
        } else if (REG_USER_IDP.equals(regId)) {
            targetUrl = CUSTOMER_FE_URL;
        } else {
            targetUrl = CUSTOMER_FE_URL;
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

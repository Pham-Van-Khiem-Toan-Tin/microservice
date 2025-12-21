package com.ecommerce.authservice.config;

import com.ecommerce.authservice.security.HttpCookieOAuth2AuthorizationRequestRepository;
import com.ecommerce.authservice.utils.CookieUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ecommerce.authservice.security.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Autowired
    private HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    // 1. Inject Repository để lấy token
    @Autowired
    private OAuth2AuthorizedClientRepository authorizedClientRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // --- PHẦN THÊM MỚI: LẤY TOKEN VÀ LƯU VÀO SESSION ---
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            // Lấy thông tin Authorized Client (chứa access token, refresh token...)
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
                // Lấy Access Token String và lưu vào Session
                // KEY NÀY PHẢI TRÙNG VỚI KEY BẠN DÙNG Ở GATEWAY
                HttpSession session = request.getSession();
                session.setAttribute("AUTHORITIES", rolesJson);
                session.setAttribute("BFF_ACCESS_TOKEN", accessToken);
            }
        }
        // --- HẾT PHẦN THÊM MỚI ---

        // 2. Lấy URL mà Frontend muốn quay về
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        // 3. Xóa các Cookie tạm
        clearAuthenticationAttributes(request, response);

        // 4. Redirect về Frontend
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Optional<String> redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

        String targetUrl = redirectUri.orElse("http://localhost:5173/");

        return UriComponentsBuilder.fromUriString(targetUrl)
                .build().toUriString();
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequest(request, response);
    }
}

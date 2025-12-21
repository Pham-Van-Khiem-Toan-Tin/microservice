package com.ecommerce.authservice.security;

import com.ecommerce.authservice.utils.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class HttpCookieOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    // Tên cookie lưu trạng thái OAuth2 (State)
    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";

    // Tên cookie lưu link frontend muốn redirect về (để dùng sau khi login xong)
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";

    // Thời gian sống của cookie (180 giây = 3 phút là đủ để user đăng nhập)
    private static final int cookieExpireSeconds = 180;

    // 1. Tải thông tin Request từ Cookie (Lúc Callback về)
    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return CookieUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
                .map(cookie -> CookieUtils.deserialize(cookie, OAuth2AuthorizationRequest.class))
                .orElse(null);
    }

    // 2. Lưu thông tin Request vào Cookie (Lúc bắt đầu Redirect đi)
    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            removeAuthorizationRequestCookies(request, response);
            return;
        }

        // Lưu object OAuth2AuthorizationRequest vào cookie
        CookieUtils.addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, CookieUtils.serialize(authorizationRequest), cookieExpireSeconds);

        // Kiểm tra xem Frontend có gửi kèm tham số "redirect_uri" không?
        String redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
        if (StringUtils.hasText(redirectUriAfterLogin)) {
            // Nếu có, lưu luôn vào cookie để dùng sau
            CookieUtils.addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME, redirectUriAfterLogin, cookieExpireSeconds);
        }
    }

    // 3. Xóa thông tin Request (Lúc đã xử lý xong hoặc hủy)
    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        OAuth2AuthorizationRequest authorizationRequest = this.loadAuthorizationRequest(request);
        removeAuthorizationRequestCookies(request, response);
        return authorizationRequest;
    }

    // Hàm phụ để xóa sạch các cookie liên quan
    public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
    }
}

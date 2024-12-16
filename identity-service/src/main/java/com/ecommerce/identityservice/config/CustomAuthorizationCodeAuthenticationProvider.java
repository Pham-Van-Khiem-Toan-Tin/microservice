package com.ecommerce.identityservice.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.log.LogMessage;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.*;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.*;
import java.util.function.Consumer;



public class CustomAuthorizationCodeAuthenticationProvider implements AuthenticationProvider  {


    private static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";
    private static final OAuth2TokenType AUTHORIZATION_CODE_TOKEN_TYPE = new OAuth2TokenType("code");
    private static final OAuth2TokenType ID_TOKEN_TOKEN_TYPE = new OAuth2TokenType("id_token");
    private final Log logger = LogFactory.getLog(this.getClass());
    private final OAuth2AuthorizationService authorizationService;
    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;
    private SessionRegistry sessionRegistry;

    public CustomAuthorizationCodeAuthenticationProvider(OAuth2AuthorizationService authorizationService, OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator) {
        Assert.notNull(authorizationService, "authorizationService cannot be null");
        Assert.notNull(tokenGenerator, "tokenGenerator cannot be null");
        this.authorizationService = authorizationService;
        this.tokenGenerator = tokenGenerator;
    }

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OAuth2AuthorizationCodeAuthenticationToken authorizationCodeAuthentication = (OAuth2AuthorizationCodeAuthenticationToken)authentication;
        OAuth2ClientAuthenticationToken clientPrincipal = CustomOAuth2AuthenticationProviderUtils.getAuthenticatedClientElseThrowInvalidClient(authorizationCodeAuthentication);
        RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();
        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Retrieved registered client");
        }

        OAuth2Authorization authorization = this.authorizationService.findByToken(authorizationCodeAuthentication.getCode(), AUTHORIZATION_CODE_TOKEN_TYPE);
        if (authorization == null) {
            throw new OAuth2AuthenticationException("invalid_grant");
        } else {
            if (this.logger.isTraceEnabled()) {
                this.logger.trace("Retrieved authorization with authorization code");
            }

            OAuth2Authorization.Token<OAuth2AuthorizationCode> authorizationCode = authorization.getToken(OAuth2AuthorizationCode.class);
            OAuth2AuthorizationRequest authorizationRequest = (OAuth2AuthorizationRequest)authorization.getAttribute(OAuth2AuthorizationRequest.class.getName());
            if (!registeredClient.getClientId().equals(authorizationRequest.getClientId())) {
                if (!authorizationCode.isInvalidated()) {
                    authorization = CustomOAuth2AuthenticationProviderUtils.invalidate(authorization, (OAuth2AuthorizationCode)authorizationCode.getToken());
                    this.authorizationService.save(authorization);
                    if (this.logger.isWarnEnabled()) {
                        this.logger.warn(LogMessage.format("Invalidated authorization code used by registered client '%s'", registeredClient.getId()));
                    }
                }

                throw new OAuth2AuthenticationException("invalid_grant");
            } else if (StringUtils.hasText(authorizationRequest.getRedirectUri()) && !authorizationRequest.getRedirectUri().equals(authorizationCodeAuthentication.getRedirectUri())) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug(LogMessage.format("Invalid request: redirect_uri does not match for registered client '%s'", registeredClient.getId()));
                }

                throw new OAuth2AuthenticationException("invalid_grant");
            } else if (!authorizationCode.isActive()) {
                if (authorizationCode.isInvalidated()) {
                    OAuth2Authorization.Token<? extends OAuth2Token> token = authorization.getRefreshToken() != null ? authorization.getRefreshToken() : authorization.getAccessToken();
                    if (token != null) {
                        authorization = CustomOAuth2AuthenticationProviderUtils.invalidate(authorization, token.getToken());
                        this.authorizationService.save(authorization);
                        if (this.logger.isWarnEnabled()) {
                            this.logger.warn(LogMessage.format("Invalidated authorization token(s) previously issued to registered client '%s'", registeredClient.getId()));
                        }
                    }
                }

                throw new OAuth2AuthenticationException("invalid_grant");
            } else {
                if (this.logger.isTraceEnabled()) {
                    this.logger.trace("Validated token request parameters");
                }

                Authentication principal = (Authentication)authorization.getAttribute(Principal.class.getName());
                DefaultOAuth2TokenContext.Builder tokenContextBuilder = (DefaultOAuth2TokenContext.Builder)((DefaultOAuth2TokenContext.Builder)((DefaultOAuth2TokenContext.Builder)((DefaultOAuth2TokenContext.Builder)((DefaultOAuth2TokenContext.Builder)((DefaultOAuth2TokenContext.Builder)((DefaultOAuth2TokenContext.Builder)DefaultOAuth2TokenContext.builder().registeredClient(registeredClient)).principal(principal)).authorizationServerContext(AuthorizationServerContextHolder.getContext())).authorization(authorization)).authorizedScopes(authorization.getAuthorizedScopes())).authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)).authorizationGrant(authorizationCodeAuthentication);
                OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.from(authorization);
                OAuth2TokenContext tokenContext = ((DefaultOAuth2TokenContext.Builder)tokenContextBuilder.tokenType(OAuth2TokenType.ACCESS_TOKEN)).build();
                OAuth2Token generatedAccessToken = this.tokenGenerator.generate(tokenContext);
                if (generatedAccessToken == null) {
                    OAuth2Error error = new OAuth2Error("server_error", "The token generator failed to generate the access token.", "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2");
                    throw new OAuth2AuthenticationException(error);
                } else {
                    if (this.logger.isTraceEnabled()) {
                        this.logger.trace("Generated access token");
                    }

                    OAuth2AccessToken accessToken = CustomOAuth2AuthenticationProviderUtils.accessToken(authorizationBuilder, generatedAccessToken, tokenContext);
                    OAuth2RefreshToken refreshToken = null;
                    if (registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN)) {
                        tokenContext = ((DefaultOAuth2TokenContext.Builder)tokenContextBuilder.tokenType(OAuth2TokenType.REFRESH_TOKEN)).build();
                        OAuth2Token generatedRefreshToken = this.tokenGenerator.generate(tokenContext);
                        if (generatedRefreshToken != null) {
                            if (!(generatedRefreshToken instanceof OAuth2RefreshToken)) {
                                OAuth2Error error = new OAuth2Error("server_error", "The token generator failed to generate a valid refresh token.", "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2");
                                throw new OAuth2AuthenticationException(error);
                            }

                            if (this.logger.isTraceEnabled()) {
                                this.logger.trace("Generated refresh token");
                            }

                            refreshToken = (OAuth2RefreshToken)generatedRefreshToken;
                            authorizationBuilder.refreshToken(refreshToken);
                        }
                    }

                    OidcIdToken idToken;
                    if (authorizationRequest.getScopes().contains("openid")) {
                        SessionInformation sessionInformation = this.getSessionInformation(principal);
                        OAuth2Error error;
                        if (sessionInformation != null) {
                            try {
                                sessionInformation = new SessionInformation(sessionInformation.getPrincipal(), createHash(sessionInformation.getSessionId()), sessionInformation.getLastRequest());
                            } catch (NoSuchAlgorithmException var19) {
                                error = new OAuth2Error("server_error", "Failed to compute hash for Session ID.", "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2");
                                throw new OAuth2AuthenticationException(error);
                            }

                            tokenContextBuilder.put(SessionInformation.class, sessionInformation);
                        }

                        tokenContext = ((DefaultOAuth2TokenContext.Builder)((DefaultOAuth2TokenContext.Builder)tokenContextBuilder.tokenType(ID_TOKEN_TOKEN_TYPE)).authorization(authorizationBuilder.build())).build();
                        OAuth2Token generatedIdToken = this.tokenGenerator.generate(tokenContext);
                        if (!(generatedIdToken instanceof Jwt)) {
                            error = new OAuth2Error("server_error", "The token generator failed to generate the ID token.", "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2");
                            throw new OAuth2AuthenticationException(error);
                        }

                        if (this.logger.isTraceEnabled()) {
                            this.logger.trace("Generated id token");
                        }

                        idToken = new OidcIdToken(generatedIdToken.getTokenValue(), generatedIdToken.getIssuedAt(), generatedIdToken.getExpiresAt(), ((Jwt)generatedIdToken).getClaims());
                        authorizationBuilder.token(idToken, (metadata) -> {
                            metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, idToken.getClaims());
                        });
                    } else {
                        idToken = null;
                    }

                    authorization = authorizationBuilder.build();
                    authorization = CustomOAuth2AuthenticationProviderUtils.invalidate(authorization, (OAuth2AuthorizationCode)authorizationCode.getToken());
                    this.authorizationService.save(authorization);
                    if (this.logger.isTraceEnabled()) {
                        this.logger.trace("Saved authorization");
                    }

                    Map<String, Object> additionalParameters = Collections.emptyMap();
                    if (idToken != null) {
                        additionalParameters = new HashMap();
                        ((Map)additionalParameters).put("id_token", idToken.getTokenValue());
                    }

                    if (this.logger.isTraceEnabled()) {
                        this.logger.trace("Authenticated token request");
                    }

                    return new OAuth2AccessTokenAuthenticationToken(registeredClient, clientPrincipal, accessToken, refreshToken, (Map)additionalParameters);
                }
            }
        }
    }

    public boolean supports(Class<?> authentication) {
        return OAuth2AuthorizationCodeAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public void setSessionRegistry(SessionRegistry sessionRegistry) {
        Assert.notNull(sessionRegistry, "sessionRegistry cannot be null");
        this.sessionRegistry = sessionRegistry;
    }

    private SessionInformation getSessionInformation(Authentication principal) {
        SessionInformation sessionInformation = null;
        if (this.sessionRegistry != null) {
            List<SessionInformation> sessions = this.sessionRegistry.getAllSessions(principal.getPrincipal(), false);
            if (!CollectionUtils.isEmpty(sessions)) {
                sessionInformation = sessions.get(0);
                if (sessions.size() > 1) {
                    List<SessionInformation> sortedSessions = new ArrayList<>(sessions);
                    sortedSessions.sort(Comparator.comparing(SessionInformation::getLastRequest));
                    sessionInformation = sortedSessions.get(sortedSessions.size() - 1);
                }
            }
        }

        return sessionInformation;
    }

    private static String createHash(String value) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(value.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }
}

package jj.study.auctionbackend.repository.security;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jj.study.auctionbackend.common.env.CustomJwtProperties;
import jj.study.auctionbackend.config.security.util.CustomCookieUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;


@Slf4j
public class OAuth2AuthorizationRequestBasedOnCookieRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private final CustomJwtProperties customJwtProperties;

    public OAuth2AuthorizationRequestBasedOnCookieRepository(CustomJwtProperties customJwtProperties) {
        this.customJwtProperties = customJwtProperties;
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        log.debug("request := [{}]", request);

        return CustomCookieUtil.getCookie(request, customJwtProperties.getOauth2AuthorizationRequestCookieName())
                .map(cookie -> CustomCookieUtil.deserialize(cookie, OAuth2AuthorizationRequest.class))
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        log.debug("authorizationRequest := [{}]", authorizationRequest);

        if (authorizationRequest == null) {
            CustomCookieUtil.deleteCookie(request, response, customJwtProperties.getOauth2AuthorizationRequestCookieName());
            CustomCookieUtil.deleteCookie(request, response, customJwtProperties.getRedirectUriParamCookieName());
            CustomCookieUtil.deleteCookie(request, response, customJwtProperties.getRefreshToken());
            return;
        }

        CustomCookieUtil.addCookie(response, customJwtProperties.getOauth2AuthorizationRequestCookieName(), CustomCookieUtil.serialize(authorizationRequest), customJwtProperties.getCookieExpireSeconds());
        String redirectUriAfterLogin = request.getParameter(customJwtProperties.getRedirectUriParamCookieName());
        if (StringUtils.isNotBlank(redirectUriAfterLogin)) {
            CustomCookieUtil.addCookie(response, customJwtProperties.getRedirectUriParamCookieName(), redirectUriAfterLogin, customJwtProperties.getCookieExpireSeconds());
        }
    }


    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        log.debug("request := [{}]", request);

        return this.loadAuthorizationRequest(request);
    }

    /**
     * Removte a cookie which is generated during authentication process
     *
     * @param request
     * @param response
     */
    public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        log.debug("request := [{}]", request);

        CustomCookieUtil.deleteCookie(request, response, customJwtProperties.getOauth2AuthorizationRequestCookieName());
        CustomCookieUtil.deleteCookie(request, response, customJwtProperties.getRedirectUriParamCookieName());
        CustomCookieUtil.deleteCookie(request, response, customJwtProperties.getRefreshToken());
    }


}

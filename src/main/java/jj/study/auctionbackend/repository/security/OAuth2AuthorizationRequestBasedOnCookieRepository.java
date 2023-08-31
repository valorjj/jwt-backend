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
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler;

import java.io.IOException;


@Slf4j
public class OAuth2AuthorizationRequestBasedOnCookieRepository extends AbstractAuthenticationTargetUrlRequestHandler implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

	private final CustomJwtProperties customJwtProperties;

	public OAuth2AuthorizationRequestBasedOnCookieRepository(CustomJwtProperties customJwtProperties) {
		this.customJwtProperties = customJwtProperties;
	}

	@Override
	public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
		log.debug("[+] request id := [{}]", request.getRequestId());
		log.debug("[+] request uri := [{}]", request.getRequestURI());

		return CustomCookieUtil.getCookie(request, customJwtProperties.getOauth2AuthorizationRequestCookieName())
				.map(cookie -> CustomCookieUtil.deserialize(cookie, OAuth2AuthorizationRequest.class))
				.orElse(null);
	}

	/**
	 * @param authorizationRequest the {@link OAuth2AuthorizationRequest}
	 * @param request              the {@code HttpServletRequest}
	 * @param response             the {@code HttpServletResponse}
	 */
	@Override
	public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
		log.debug("[+] authorizationRequest client id:= [{}]", authorizationRequest.getClientId());
		log.debug("[+] getAuthorizationUri := [{}]", authorizationRequest.getAuthorizationUri());
		log.debug("[+] getAuthorizationRequestUri := [{}]", authorizationRequest.getAuthorizationRequestUri());

		//
		if (authorizationRequest == null) {
			CustomCookieUtil.deleteCookie(request, response, customJwtProperties.getOauth2AuthorizationRequestCookieName());
			CustomCookieUtil.deleteCookie(request, response, customJwtProperties.getRedirectUriParamCookieName());
			CustomCookieUtil.deleteCookie(request, response, customJwtProperties.getRefreshToken());
			return;
		}

		// AccessToken 발급을 위해서 인가서버로 요청을 보내는 작업을 하기 직전단계이다.
		// 이 다음에 어디로 가는지가 의문이다.

		// AccessToken 발급 요청 시 Cookie 에 담는다.
		// response 에 쿠키를 담아서, 어느 시점에 인가서버로 전송하는거야? 어떤 객체를 봐야해?
		CustomCookieUtil.addCookie(response, customJwtProperties.getOauth2AuthorizationRequestCookieName(), CustomCookieUtil.serialize(authorizationRequest), customJwtProperties.getCookieExpireSeconds());
		// 여기서 redirect 쿠키가 있는지 확인하는데, 첫 로그인시에는 그런거 없다.
		String redirectUriAfterLogin = request.getParameter(customJwtProperties.getRedirectUriParamCookieName());
		log.debug("[+] redirectUriAfterLogin:= [{}]", redirectUriAfterLogin);

		// 이후 단계가 대체 뭐야? 로그에 아무것도 안찍혀
		log.info("[+] response committed? := [{}]", response.isCommitted());
		log.info("[+] response status? := [{}]", response.getStatus());


		// 가장 첫 로그인 시 쿠키가 존재하지 않는다.
		// 로그인 이후 자원 요청 시 거치는 과정인듯 하다.
		if (StringUtils.isNotBlank(redirectUriAfterLogin)) {
			CustomCookieUtil.addCookie(response, customJwtProperties.getRedirectUriParamCookieName(), redirectUriAfterLogin, customJwtProperties.getCookieExpireSeconds());
		}
	}


	@Override
	public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
		log.debug("[+] request := [{}]", request);
		return this.loadAuthorizationRequest(request);
	}

	/**
	 * Removte a cookie which is generated during authentication process
	 *
	 * @param request
	 * @param response
	 */
	public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
		log.debug("[+] request := [{}]", request);

		CustomCookieUtil.deleteCookie(request, response, customJwtProperties.getOauth2AuthorizationRequestCookieName());
		CustomCookieUtil.deleteCookie(request, response, customJwtProperties.getRedirectUriParamCookieName());
		CustomCookieUtil.deleteCookie(request, response, customJwtProperties.getRefreshToken());
	}

	@Override
	protected RedirectStrategy getRedirectStrategy() {
		return super.getRedirectStrategy();
	}
}

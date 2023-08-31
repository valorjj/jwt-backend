package jj.study.auctionbackend.config.security.handler;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jj.study.auctionbackend.common.env.CustomJwtProperties;
import jj.study.auctionbackend.common.env.CustomOAuth2Properties;
import jj.study.auctionbackend.config.security.exception.CustomOAuth2Exception;
import jj.study.auctionbackend.config.security.exception.CustomTokenException;
import jj.study.auctionbackend.config.security.oauth2.OAuth2UserInfo;
import jj.study.auctionbackend.config.security.oauth2.OAuth2UserInfoFactory;
import jj.study.auctionbackend.config.security.provider.AuthTokenProvider;
import jj.study.auctionbackend.config.security.util.CustomCookieUtil;
import jj.study.auctionbackend.domain.token.AuthToken;
import jj.study.auctionbackend.domain.token.entity.UserRefreshToken;
import jj.study.auctionbackend.domain.user.enums.ProviderType;
import jj.study.auctionbackend.domain.user.enums.RoleType;
import jj.study.auctionbackend.repository.security.OAuth2AuthorizationRequestBasedOnCookieRepository;
import jj.study.auctionbackend.repository.security.UserRefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;


@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final AuthTokenProvider authTokenProvider;
	private final UserRefreshTokenRepository userRefreshTokenRepository;
	private final OAuth2AuthorizationRequestBasedOnCookieRepository auth2AuthorizationRequestBasedOnCookieRepository;
	private final CustomJwtProperties customJwtProperties;
	private final CustomOAuth2Properties customOAuth2Properties;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
		log.info("[+] Authentication Successful Handler 에 접근");
		// (1)
		String targetUrl = determineTargetUrl(request, response, authentication);
		// (2) When response already has a status code and header
		if (response.isCommitted()) {
			log.debug("Response has been already committed. Unable to redirect to " + targetUrl);
			return;
		}
		// (3) Remove related session and cookie
		clearAuthentication(request, response);
		// (4)
		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}

	/**
	 * Remove temporal session and cookie which used during authentication process
	 *
	 * @param request
	 * @param response
	 */
	private void clearAuthentication(HttpServletRequest request, HttpServletResponse response) {
		log.info("[+] clearAuthentication 접근");
		// (1) Remove temporal session data related to authentication data
		super.clearAuthenticationAttributes(request);
		// (2) Remove a cookie used in authentication process
		auth2AuthorizationRequestBasedOnCookieRepository.removeAuthorizationRequestCookies(request, response);
	}

	/**
	 * Create an access token and refresh token. Then load the access token in query string parameters
	 *
	 * @param request
	 * @param response
	 * @param authentication
	 * @return
	 */
	@Override
	protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		log.info("[+] determineTargetUrl 접근");
		// (1)
		Optional<String> redirectUri = CustomCookieUtil.getCookie(request, customJwtProperties.getRedirectUriParamCookieName()).map(Cookie::getValue);
		log.info("[+] redirectUri := [{}]", redirectUri);
		// (2)
		if (redirectUri.isPresent() && isAuthorizedRedirectUri(redirectUri.get())) {
			throw new CustomOAuth2Exception("Unable to process the authentication because redirectUri is not authorized");
		}
		// (3)
		String targetUrl = redirectUri.orElse(getDefaultTargetUrl());
		log.info("[+] targetUrl := [{}]", targetUrl);
		// (4)
		OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
		// (5)
		ProviderType providerType = ProviderType.valueOf(authToken.getAuthorizedClientRegistrationId().toUpperCase());
		// (6)
		OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
		// (7)
		Collection<? extends GrantedAuthority> authorities = oidcUser.getAuthorities();
		// (8)
		OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(providerType, oidcUser.getAttributes());
		// (9)
		RoleType roleType = hasAuthority(authorities, RoleType.ADMIN.getCode()) ? RoleType.ADMIN : RoleType.USER;
		// (10-try)
		Date now = new Date();
		long accessTokenExpirationTime = now.getTime() + customJwtProperties.getAccessTokenValidationTime();
		long refreshTokenExpirationTime = now.getTime() + customJwtProperties.getRefreshTokenValidationTime();
		// (11) Create an access token and refresh token
		// NOTE: Date is now deprecated, but not sure this will work
		AuthToken accessToken = authTokenProvider.createAuthToken(userInfo.getId(), roleType.getCode(), new Date(accessTokenExpirationTime));
		AuthToken refreshToken = authTokenProvider.createAuthToken(customJwtProperties.getSecret(), new Date(refreshTokenExpirationTime));
		// (12)
		Optional<UserRefreshToken> userRefreshToken = userRefreshTokenRepository.findByUserId(userInfo.getId());
		if (userRefreshToken.isEmpty()) {
			// (12-1)
			userRefreshTokenRepository.saveAndFlush(new UserRefreshToken(userInfo.getId(), refreshToken.getToken()));
		} else {
			// (12-2)
			userRefreshToken.get().setRefreshToken(refreshToken.getToken());
		}
		// (13)
		CustomCookieUtil.deleteCookie(request, response, customJwtProperties.getRefreshToken());
		CustomCookieUtil.addCookie(response, customJwtProperties.getRefreshToken(),
				refreshToken.getToken(), (int) refreshTokenExpirationTime
		);
		// (14) 아래의 코드로 이동해서 인가서버로부터 Access Token 을 획득할 코드를 얻는다.
		String uriToExternalAuthorizationServer = UriComponentsBuilder.fromUriString(targetUrl).queryParam("token", accessToken.getToken())
				.build().toUriString();
		log.info("[+] uriToeExternalAuthorizationServer: [{}]", uriToExternalAuthorizationServer);

		// (15) Rest Template 을 통해서 인가서버와 통신한다.
		ResponseEntity<String> res = new RestTemplate().getForEntity(uriToExternalAuthorizationServer, String.class);
		log.info("[+] res := [{}}", res);

		return "http://localhost:3000/";
	}


	public Boolean hasAuthority(Collection<? extends GrantedAuthority> authorities, String authority) {
		// (1)
		if (authorities == null) {
			return false;
		}
		// (2)
		for (GrantedAuthority grantedAuthority : authorities) {
			// (2-1)
			if (authority.equals(grantedAuthority.getAuthority())) {
				return true;
			}
		}
		return false;
	}

	public Boolean isAuthorizedRedirectUri(String uri) {
		// (1-try)
		try {
			// (1-1)
			URL clientRedirectUri = URI.create(uri).toURL();
			// (1-2)
			return customOAuth2Properties.getAuthorizedRedirectUris().stream()
					.anyMatch(authorizedRedirectUri -> {
						URI authorizedURI = URI.create(authorizedRedirectUri);
						return authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
								&& authorizedURI.getPort() == clientRedirectUri.getPort();
					});
		}
		// (1-catch)
		catch (MalformedURLException | IllegalArgumentException e) {
			log.info("An error occurred. Message := {}", e.getLocalizedMessage());
			throw new CustomTokenException("유효하지 않은 url 이 입력되었습니다.");
		}
	}
}

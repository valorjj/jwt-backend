package jj.study.auctionbackend.config.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jj.study.auctionbackend.common.env.CustomJwtProperties;
import jj.study.auctionbackend.config.security.util.CustomCookieUtil;
import jj.study.auctionbackend.repository.security.OAuth2AuthorizationRequestBasedOnCookieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;


@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final OAuth2AuthorizationRequestBasedOnCookieRepository auth2AuthorizationRequestBasedOnCookieRepository;
    private final CustomJwtProperties customJwtProperties;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        // (1)
        String targetUrl = CustomCookieUtil.getCookie(request, customJwtProperties.getRedirectUriParamCookieName())
                .map(Cookie::getValue)
                .orElse(("/"));
        log.info("An error occurred. Message := {}", exception.getMessage());
        // (2)
        targetUrl = UriComponentsBuilder.fromUriString(targetUrl).queryParam("error", exception.getLocalizedMessage())
                .build().toUriString();
        // (3)
        auth2AuthorizationRequestBasedOnCookieRepository.removeAuthorizationRequestCookies(request, response);
        // (4)
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

}

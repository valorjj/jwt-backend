package jj.study.auctionbackend.config.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jj.study.auctionbackend.common.util.CustomHeaderUtil;
import jj.study.auctionbackend.config.security.provider.AuthTokenProvider;
import jj.study.auctionbackend.domain.token.AuthToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final AuthTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("[+] Authentication Filter 에 접근");

        // (1)
        String tokenStr = CustomHeaderUtil.getAccessToken(request);
        // (2)
        AuthToken token = tokenProvider.convertAuthToken(tokenStr);
        // (3)
        if (token.validate()) {
            // (3-1)
            Authentication authentication = tokenProvider.getAuthentication(token);
            // (3-2)
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        // (4)
        filterChain.doFilter(request, response);
    }
}

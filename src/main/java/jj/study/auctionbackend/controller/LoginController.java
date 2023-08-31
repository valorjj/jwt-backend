package jj.study.auctionbackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jj.study.auctionbackend.common.env.CustomJwtProperties;
import jj.study.auctionbackend.config.security.provider.AuthTokenProvider;
import jj.study.auctionbackend.domain.auth.AuthRequest;
import jj.study.auctionbackend.repository.security.UserRefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@Controller
@RequiredArgsConstructor
public class LoginController {


    private final CustomJwtProperties customJwtProperties;
    private final AuthTokenProvider authTokenProvider;
    private final UserRefreshTokenRepository userRefreshTokenRepository;

    @GetMapping("/login")
    public String login(HttpServletRequest request, HttpServletResponse response) {

        return "login";
    }


}

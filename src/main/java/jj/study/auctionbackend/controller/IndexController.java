package jj.study.auctionbackend.controller;


import jj.study.auctionbackend.domain.user.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class IndexController {

	@GetMapping("/")
	public String index(Authentication authentication, @AuthenticationPrincipal UserPrincipal userPrincipal) {
		log.info("authentication := [{}]", authentication);
		log.info("userPrincipal := [{}]", userPrincipal);

		return "index";
	}

}

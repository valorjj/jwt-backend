package jj.study.auctionbackend.service.user;

import jj.study.auctionbackend.config.security.oauth2.OAuth2UserInfo;
import jj.study.auctionbackend.config.security.oauth2.OAuth2UserInfoFactory;
import jj.study.auctionbackend.config.security.exception.CustomOAuth2Exception;
import jj.study.auctionbackend.domain.user.UserPrincipal;
import jj.study.auctionbackend.domain.user.entity.User;
import jj.study.auctionbackend.domain.user.enums.ProviderType;
import jj.study.auctionbackend.domain.user.enums.RoleType;
import jj.study.auctionbackend.repository.user.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class CustomOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

	private final UserJpaRepository userJpaRepository;

	@Override
	public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
		log.info("[+] OidcUserService 접근");

		// (1)
		ClientRegistration clientRegistration = userRequest.getClientRegistration();
		// (2)
		OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService = new OidcUserService();
		OidcUser oidcUser = oidcUserService.loadUser(userRequest);
		// (3)
		return this.process(userRequest, oidcUser);
	}

	private OidcUser process(OAuth2UserRequest userRequest, OidcUser user) {
		// (1) Identify an authorization provider
		ProviderType providerType = ProviderType.valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase());
		log.debug("[+] providerType := [{}]", providerType.getProviderType());
		// (2)
		OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(providerType, user.getAttributes());
		log.debug("[+] userInfo -> id := [{}]", userInfo.getId());
		log.debug("[+] userInfo -> email := [{}]", userInfo.getEmail());
		log.debug("[+] userInfo -> name := [{}]", userInfo.getName());
		log.debug("[+] userInfo -> attributes := [{}]", userInfo.getAttributes());
		// (3)
		Optional<User> userOP = userJpaRepository.findByUserId(userInfo.getId());
		if (userOP.isPresent()) {
			if (providerType != userOP.get().getProviderType()) {
				throw new CustomOAuth2Exception("[+] It seems that you are already signup with [" + providerType + "] account. Please try again with your [" + userOP.get().getProviderType() + "]");
			}
			updateUser(userOP.get(), userInfo);
		} else {
			userOP = Optional.of(createUser(userInfo, providerType));
		}

		return UserPrincipal.create(userOP.get(), user.getAttributes());
	}

	private User createUser(OAuth2UserInfo userInfo, ProviderType providerType) {
		LocalDateTime now = LocalDateTime.now();
		User user = User.builder()
				.userId(userInfo.getId())
				.username(userInfo.getName())
				.email(userInfo.getEmail())
				.emailVerifiedYn("Y")
				.profileImageUrl(userInfo.getImageUrl())
				.providerType(providerType)
				.roleType(RoleType.USER)
				.build();

		return userJpaRepository.saveAndFlush(user);
	}

	private void updateUser(User user, OAuth2UserInfo userInfo) {
		if (userInfo.getName() != null && !user.getUsername().equals(userInfo.getName())) {
			user.setUsername(userInfo.getName());
		}

		if (userInfo.getImageUrl() != null && !user.getProfileImageUrl().equals(userInfo.getImageUrl())) {
			user.setProfileImageUrl(userInfo.getImageUrl());
		}
	}

}

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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserJpaRepository userJpaRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // (1)
        OAuth2User user = super.loadUser(userRequest);
        // (2-try)
        try {
            return this.process(userRequest, user);
        }
        // (2-catch)
        catch (AuthenticationException e) {
            throw new CustomOAuth2Exception(e.getLocalizedMessage());
        } catch (Exception e) {
            e.fillInStackTrace();
            throw new CustomOAuth2Exception(e.getLocalizedMessage());
        }
    }

    private OAuth2User process(OAuth2UserRequest userRequest, OAuth2User user) {
        // (1) Identify an authorization provider
        ProviderType providerType = ProviderType.valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase());
        // (2)
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(providerType, user.getAttributes());
        // (3)
        Optional<User> userOP = userJpaRepository.findByUserId(userInfo.getId());
        if (userOP.isPresent()) {
            if (providerType != userOP.get().getProviderType()) {
                throw new CustomOAuth2Exception("It seems that you are already signup with [" + providerType + "] account. Please try again with your [" + userOP.get().getProviderType() + "]");
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

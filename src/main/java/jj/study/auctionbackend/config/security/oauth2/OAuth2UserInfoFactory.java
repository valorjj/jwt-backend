package jj.study.auctionbackend.config.security.oauth2;

import jj.study.auctionbackend.config.security.exception.CustomTokenException;
import jj.study.auctionbackend.domain.user.enums.ProviderType;
import jj.study.auctionbackend.domain.user.social.GoogleOAuth2UserInfo;
import jj.study.auctionbackend.domain.user.social.KakaoOAuth2UserInfo;
import jj.study.auctionbackend.domain.user.social.NaverOAuth2UserInfo;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(ProviderType providerType, Map<String, Object> attributes) {
        return switch (providerType) {
            case GOOGLE -> new GoogleOAuth2UserInfo(attributes);
            case KAKAO -> new KakaoOAuth2UserInfo(attributes);
            case NAVER -> new NaverOAuth2UserInfo(attributes);
            // case GITHUB -> new GithubOAuth2UserInfo(attributes);
            default -> throw new CustomTokenException("Invalid provider type: [" + providerType + "]");
        };
    }

}

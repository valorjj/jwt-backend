package jj.study.auctionbackend.domain.user.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProviderType {

    GOOGLE("google"),
    KEYCLOAK("keycloak"),
    KAKAO("kakao"),
    NAVER("naver"),
    GITHUB("github");

    private final String providerType;


}

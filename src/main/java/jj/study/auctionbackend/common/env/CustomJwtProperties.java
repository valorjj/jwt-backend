package jj.study.auctionbackend.common.env;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;


@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@PropertySource(value = "classpath:config/custom-config.yml", factory = YamlPropertySourceFactory.class)
@Getter
@Setter
public class CustomJwtProperties {

    // (1) JWT Token Secret Key
    private String secret;
    // (2) JWT Token Type: Bearer
    private String bearerType;
    // (3) Access Token`s expiration time
    private Long accessTokenValidationTime;
    // (4) Refresh Token`s expiration time
    private Long refreshTokenValidationTime;
    // (5) Cookies`s name as an authorization code
    private String oauth2AuthorizationRequestCookieName;
    // (6)
    private String redirectUriParamCookieName;
    // (7)
    private String refreshToken;
    // (8)
    private Integer cookieExpireSeconds;


}

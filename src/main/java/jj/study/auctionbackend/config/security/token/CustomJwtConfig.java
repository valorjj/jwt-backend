package jj.study.auctionbackend.config.security.token;


import jj.study.auctionbackend.common.env.CustomJwtProperties;
import jj.study.auctionbackend.config.security.provider.AuthTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CustomJwtConfig {

    private final CustomJwtProperties customJwtProperties;

    @Bean
    public AuthTokenProvider jwtProvider() {
        String secret = customJwtProperties.getSecret();
        return new AuthTokenProvider(secret);
    }

}

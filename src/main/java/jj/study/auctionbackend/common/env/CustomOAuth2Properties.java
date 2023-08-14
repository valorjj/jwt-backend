package jj.study.auctionbackend.common.env;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app.oauth2")
@PropertySource(value = "classpath:config/custom-config.yml", factory = YamlPropertySourceFactory.class)
@Getter
@Setter
public class CustomOAuth2Properties {

    private List<String> authorizedRedirectUris;

}

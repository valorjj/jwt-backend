package jj.study.auctionbackend;

import jj.study.auctionbackend.common.env.CustomJwtProperties;
import jj.study.auctionbackend.common.env.YamlPropertySourceFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@PropertySource(value = {
        "classpath:config/custom-config.yml"
}, factory = YamlPropertySourceFactory.class)
@Slf4j
public class AuctionBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuctionBackendApplication.class, args);
    }


}

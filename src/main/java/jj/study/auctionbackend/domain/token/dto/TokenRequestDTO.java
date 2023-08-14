package jj.study.auctionbackend.domain.token.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRequestDTO {

    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private Duration duration;


}

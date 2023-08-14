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
public class TokenResponseDTO {

    private String accessToken;
    private String tokenType;
    private String refreshToken;
    private Duration duration;
    private boolean isNewUser;

}

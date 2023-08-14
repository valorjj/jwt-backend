package jj.study.auctionbackend.domain.auth;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    private String id;
    private String password;
}

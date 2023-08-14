package jj.study.auctionbackend.config.security.provider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import jj.study.auctionbackend.config.security.exception.CustomTokenException;
import jj.study.auctionbackend.domain.token.AuthToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

@Slf4j
public class AuthTokenProvider {

    private final Key key;
    private static final String AUTHORITIES_KEY = "role";

    public AuthTokenProvider(String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public AuthToken createAuthToken(String id, Date expirationDate) {
        return new AuthToken(id, expirationDate, key);
    }

    public AuthToken createAuthToken(String id, String role, Date expirationDate) {
        return new AuthToken(id, role, expirationDate, key);
    }

    public AuthToken convertAuthToken(String token) {
        return new AuthToken(token, key);
    }

    /**
     * @param authToken Authorization Token
     * @return
     */
    public Authentication getAuthentication(AuthToken authToken) {
        // (1)
        if (authToken.validate()) {
            // (1-1)
            Claims claims = authToken.getTokenClaims();
            // (1-2)
            Collection<? extends GrantedAuthority> authorities =
                    Arrays.stream(new String[]{String.valueOf(claims.get(AUTHORITIES_KEY))})
                            .map(SimpleGrantedAuthority::new).toList();
            log.debug("claims subject := [{}]", claims.getSubject());
            // (1-3)
            User principalUser = new User(claims.getSubject(), "", authorities);
            // (1-4)
            return new UsernamePasswordAuthenticationToken(principalUser, authToken, authorities);
        }
        // (2)
        else {
            throw new CustomTokenException();
        }
    }


}

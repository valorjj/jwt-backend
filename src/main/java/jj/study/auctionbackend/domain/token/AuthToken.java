package jj.study.auctionbackend.domain.token;


import io.jsonwebtoken.*;
import jj.study.auctionbackend.config.security.exception.CustomTokenException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.Key;
import java.util.Date;

@Getter
@Slf4j
@RequiredArgsConstructor
public class AuthToken {


	private final String token;
	private final Key key;

	private static final String AUTHORITIES_KEY = "role";

	public AuthToken(String id, Date expirationDate, Key key) {
		this.key = key;
		this.token = createAuthToken(id, expirationDate);
	}

	public AuthToken(String id, String role, Date expirationDate, Key key) {
		this.key = key;
		this.token = createAuthToken(id, role, expirationDate);
	}

	/**
	 * @param id
	 * @param role
	 * @param expirationDate
	 * @return
	 */
	private String createAuthToken(String id, String role, Date expirationDate) {
		return Jwts.builder()
				// (1)
				.setSubject(id)
				// (2)
				.claim(AUTHORITIES_KEY, role)
				.claim("TEST", "test-claim")
				.claim("EMAIL", "test-claim-email")
				// (3)
				.signWith(key, SignatureAlgorithm.HS256)
				// (4)
				.setExpiration(expirationDate)
				// (5)
				.compact();
	}

	/**
	 * @param id
	 * @param expirationDate
	 * @return
	 */
	private String createAuthToken(String id, Date expirationDate) {
		return Jwts.builder()
				// (1)
				.setSubject(id)
				// (2)
				.signWith(key, SignatureAlgorithm.HS256)
				// (3)
				.setExpiration(expirationDate)
				// (4)
				.compact();
	}

	/**
	 * @return
	 */
	public Boolean validate() {
		log.info("[+] validate := [{}]", this.getTokenClaims());
		return this.getTokenClaims() != null;
	}

	/**
	 * @return
	 */
	public Claims getTokenClaims() {
		try {
			log.info("[+] token := [{}]", token);

			return Jwts.parserBuilder()
					// (1) JWS 의 서명을 검증하기 위한 key 를 설정한다.
					.setSigningKey(key)
					.build()
					// (2) Claims 이 담긴 Jws<Claims> 를 반환한다.
					.parseClaimsJws(token)
					// (3)
					.getBody();
		} catch (SecurityException e) {
			log.info("[+] Invalid JWT Signature");
		} catch (MalformedJwtException e) {
			log.info("[+] Invalid JWT Token");
		} catch (ExpiredJwtException e) {
			log.info("[+] Expired JWT Token");
		} catch (UnsupportedJwtException e) {
			log.info("[+] Unsupported JWT Token");
		} catch (IllegalArgumentException e) {
			log.info("[+] JWT Token compact of handler are invalid");
		}
		return null;
	}


	/**
	 * @return
	 */
	public Claims getExpiredTokenClaims() {
		try {
			Jwts.parserBuilder()
					// (1)
					.setSigningKey(key)
					.build()
					// (2)
					.parseClaimsJws(token)
					.getBody();
		} catch (ExpiredJwtException e) {
			log.info("An error occurred. Message := {}", e.getLocalizedMessage());
			throw new CustomTokenException(e.getLocalizedMessage());
		}
		return null;
	}

}

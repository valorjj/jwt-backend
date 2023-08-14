package jj.study.auctionbackend.config.security.exception;

public class CustomOAuth2Exception extends RuntimeException {

    public CustomOAuth2Exception(String message) {
        super(message);
    }
}

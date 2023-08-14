package jj.study.auctionbackend.config.security.exception;

public class CustomTokenException extends RuntimeException {

    public CustomTokenException() {
        super("Failed to generate a token");
    }

    public CustomTokenException(String message) {
        super(message);
    }


}

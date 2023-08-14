package jj.study.auctionbackend.common.handler;


import jj.study.auctionbackend.common.handler.exception.CustomApiException;
import jj.study.auctionbackend.config.security.exception.CustomOAuth2Exception;
import jj.study.auctionbackend.config.security.exception.CustomTokenException;
import jj.study.auctionbackend.domain.common.dto.ResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class CustomExceptionHandler {

    @ExceptionHandler(CustomApiException.class)
    public ResponseEntity<?> apiException(CustomApiException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDTO<>(-1, e.getMessage(), null));
    }

    @ExceptionHandler(CustomOAuth2Exception.class)
    public ResponseEntity<?> oauth2Exception(CustomOAuth2Exception e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDTO<>(-1, e.getMessage(), null));
    }

    @ExceptionHandler(CustomTokenException.class)
    public ResponseEntity<?> tokenException(CustomTokenException e) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new ResponseDTO<>(-1, e.getMessage(), null));
    }

}

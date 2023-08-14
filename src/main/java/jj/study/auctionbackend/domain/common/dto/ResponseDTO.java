package jj.study.auctionbackend.domain.common.dto;

public record ResponseDTO<T>(Integer code, String message, T data) {

}

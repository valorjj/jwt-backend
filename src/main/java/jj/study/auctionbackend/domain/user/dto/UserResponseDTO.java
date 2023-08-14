package jj.study.auctionbackend.domain.user.dto;

import jj.study.auctionbackend.domain.user.enums.ProviderType;

public class UserResponseDTO {

    public static class UserLoginResponseDTO {

        private Long id;
        private String username;
        private String email;
        private ProviderType provider;
    }


}

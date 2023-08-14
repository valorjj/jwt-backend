package jj.study.auctionbackend.domain.user.social;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jj.study.auctionbackend.config.security.oauth2.OAuth2UserInfo;

import java.util.Map;

public class NaverOAuth2UserInfo extends OAuth2UserInfo {

    public NaverOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> response = objectMapper.convertValue(attributes.get("response"), new TypeReference<>() {
        });
        if (response == null) {
            return null;
        }

        return String.valueOf(response.get("id"));
    }

    @Override
    public String getName() {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> response = objectMapper.convertValue(attributes.get("response"), new TypeReference<>() {
        });
        if (response == null) {
            return null;
        }

        return String.valueOf(response.get("nickname"));
    }

    @Override
    public String getEmail() {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> response = objectMapper.convertValue(attributes.get("response"), new TypeReference<>() {
        });
        if (response == null) {
            return null;
        }

        return String.valueOf(response.get("email"));
    }

    @Override
    public String getImageUrl() {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> response = objectMapper.convertValue(attributes.get("response"), new TypeReference<>() {
        });
        if (response == null) {
            return null;
        }

        return String.valueOf(response.get("profile_image"));
    }
}

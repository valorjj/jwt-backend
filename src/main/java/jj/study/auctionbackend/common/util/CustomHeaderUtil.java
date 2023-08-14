package jj.study.auctionbackend.common.util;

import jakarta.servlet.http.HttpServletRequest;

public class CustomHeaderUtil {

    private final static String HEADER_AUTHORIZATION= "Authorization";
    private final static String TOKEN_PREFIX = "Bearer ";

    /**
     * @param request
     * @return
     */
    public static String getAccessToken(HttpServletRequest request) {
        // (1)
        String headerValue = request.getHeader(HEADER_AUTHORIZATION);

        // (2)
        if (headerValue == null) {
            return null;
        }

        // (3)
        if (headerValue.startsWith(TOKEN_PREFIX)) {
            return headerValue.substring(TOKEN_PREFIX.length());
        }

        // (4)
        return null;
    }

}

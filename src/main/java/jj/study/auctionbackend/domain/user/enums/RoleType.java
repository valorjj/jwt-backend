package jj.study.auctionbackend.domain.user.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum RoleType {

    GUEST("ROLE_GUEST", "게스트 권한"),
    USER("ROLE_USER", "일반 사용자 권한"),
    ADMIN("ROLE_ADMIN", "관리자 권한");

    private final String code;
    private final String displayName;

    public static RoleType of(String code) {
        return Arrays
                // (1)
                .stream(RoleType.values())
                // (2)
                .filter(r -> r.getCode().equals(code))
                // (3)
                .findAny()
                // (4)
                .orElse(GUEST);
    }

}

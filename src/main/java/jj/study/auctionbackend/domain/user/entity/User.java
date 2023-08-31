package jj.study.auctionbackend.domain.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jj.study.auctionbackend.domain.common.BaseEntity;
import jj.study.auctionbackend.domain.user.enums.ProviderType;
import jj.study.auctionbackend.domain.user.enums.RoleType;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "USER_TB")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_SEQ")
    private Long userSeq;

    @Column(name = "USER_ID", length = 64, unique = true)
    @NotNull
    @Size(max = 64)
    private String userId;

    @Column(name = "USERNAME", nullable = false, length = 64)
    @NotNull
    @Size(max = 64)
    private String username;

    @Email
    @Column(name = "EMAIL", length = 512, unique = true)
    @Size(max = 128)
    @NotNull
    private String email;

    @Column(name = "PASSWORD", length = 64)
    @NotNull
    @Size(max = 64)
    @JsonIgnore
    private String password;

    @Column(name = "EMAIL_VERIFIED_YN", length = 1)
    @Size(min = 1, max = 1)
    @NotNull
    private String emailVerifiedYn;

    @Column(name = "PROFILE_IMAGE_URL", length = 512)
    @Size(max = 512)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "PROVIDER_TYPE", length = 32)
    @NotNull
    private ProviderType providerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE_TYPE", length = 16)
    @NotNull
    private RoleType roleType;


    @Builder
    public User(
            @NotNull @Size(max = 64) String userId,
            @NotNull @Size(max = 64) String username,
            @NotNull @Size(max = 512) String email,
            @NotNull @Size(max = 1) String emailVerifiedYn,
            @NotNull @Size(max = 512) String profileImageUrl,
            @NotNull ProviderType providerType,
            @NotNull RoleType roleType
    ) {
        this.userId = userId;
        this.username = username;
        this.email = email != null ? email : "NO_EMAIL";
        this.password = "NO_PASSWORD";
        this.emailVerifiedYn = emailVerifiedYn;
        this.profileImageUrl = profileImageUrl != null ? profileImageUrl : "";
        this.providerType = providerType;
        this.roleType = roleType;
    }

}

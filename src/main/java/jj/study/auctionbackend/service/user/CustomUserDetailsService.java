package jj.study.auctionbackend.service.user;

import jj.study.auctionbackend.common.handler.exception.CustomApiException;
import jj.study.auctionbackend.domain.user.UserPrincipal;
import jj.study.auctionbackend.domain.user.entity.User;
import jj.study.auctionbackend.repository.user.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserJpaRepository userJpaRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // (1)
        Optional<User> userOP = userJpaRepository.findByUserId(username);
        // (2)
        User user = userOP.orElseThrow(() -> new CustomApiException("[" + username + "] was not able to find"));
        // (3)
        return UserPrincipal.create(user);
    }
}

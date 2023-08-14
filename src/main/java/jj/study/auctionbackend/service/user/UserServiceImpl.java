package jj.study.auctionbackend.service.user;

import jj.study.auctionbackend.common.handler.exception.CustomApiException;
import jj.study.auctionbackend.domain.user.entity.User;
import jj.study.auctionbackend.repository.user.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserJpaRepository userJpaRepository;

    @Override
    public User getUser(String userId) {
        Optional<User> userOP = userJpaRepository.findByUserId(userId);
        if (userOP.isPresent()) {
            return userOP.get();
        }
        throw new CustomApiException("[" + userId + "] does not exist");
    }
}

package com.example.auth;

import com.example.user.User;
import com.example.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    private final UserRepository userRepository;

    public CurrentUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User get(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow();
    }
}

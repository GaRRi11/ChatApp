package com.gary.ChatApp.web.security;

import com.gary.ChatApp.storage.model.user.User;
import com.gary.ChatApp.storage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserAuthenticationManager {

    private final UserRepository userRepository;
    private final UserPasswordEncoder userPasswordEncoder;

    public boolean authenticate (String username, String password){
        User user = userRepository.findByName(username).orElseThrow(
                () -> new IllegalArgumentException());
        return user.getPassword().equals(userPasswordEncoder.encode(password));
    }
}

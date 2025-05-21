package com.gary.ChatApp.web.dto;

import com.gary.ChatApp.domain.model.user.User;
import com.gary.ChatApp.web.security.UserPasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDTOMapper {

    private final UserPasswordEncoder userPasswordEncoder;
    public User fromDTO (UserRequest userRequest){
        return new User(
                userRequest.getName(),
                userPasswordEncoder.encode(userRequest.getPassword())
        );
    }
}

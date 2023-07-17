package com.gary.ChatApp.web.dto;

import com.gary.ChatApp.storage.model.user.User;
import org.springframework.stereotype.Component;

@Component
public class UserDTOMapper {
    public User fromDTO (UserRequest userRequest){
        return new User(
                userRequest.getName(),
                userRequest.getPassword()
        );
    }
}

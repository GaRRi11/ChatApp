package com.gary.ChatApp.web.dto;

import com.gary.ChatApp.domain.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class UserDto {

    private String username;
    private boolean online;

    private final UserPasswordEncoder userPasswordEncoder;

    public User fromDTO (AuthRequest authRequest){
        return new User(
                authRequest.getName(),
                userPasswordEncoder.encode(authRequest.getPassword())
        );
    }

    public static UserDto fromEntity(User user){
        return UserDto.builder()
                .username(user.getUsername())
                .online(user.isOnline())
                .build();
    }
}


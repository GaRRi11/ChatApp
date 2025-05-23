package com.gary.ChatApp.web.dto;

import com.gary.ChatApp.domain.model.user.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserDto(
        @NotBlank(message = "Username must not be blank")
        String username,

        @NotNull (message = "Online status must be provided")
        Boolean online
) {
    public static UserDto fromEntity(User user) {
        return new UserDto(
                user.getUsername(),
                user.isOnline()
        );
    }
}

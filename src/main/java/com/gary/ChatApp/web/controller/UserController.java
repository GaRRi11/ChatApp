package com.gary.ChatApp.web.controller;

import com.gary.ChatApp.domain.model.user.User;
import com.gary.ChatApp.domain.service.user.UserService;
import com.gary.ChatApp.exceptions.ResourceNotFoundException;
import com.gary.ChatApp.web.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        User user = userService.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return UserDto.fromEntity(user);
    }

    @GetMapping("/online")
    public List<UserDto> getOnlineUsers() {
        return userService.getOnlineUsers().stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

}

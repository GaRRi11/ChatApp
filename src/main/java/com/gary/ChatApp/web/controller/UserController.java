package com.gary.ChatApp.web.controller;

import com.gary.ChatApp.domain.model.user.User;
import com.gary.ChatApp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/online")
    public List<User> getOnlineUsers() {
        return userService.getOnlineUsers();
    }

    @PostMapping("/{id}/status")
    public void setOnlineStatus(@PathVariable Long id, @RequestParam boolean online) {
        userService.setOnlineStatus(id, online);
    }
}

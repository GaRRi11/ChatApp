package com.gary.ChatApp.domain.service.user;

import com.gary.ChatApp.domain.model.user.User;
import com.gary.ChatApp.web.dto.LoginResponse;

import java.util.Optional;
import java.util.List;

public interface UserService {
    Optional<User> getById(Long id);
    Optional<User> getByName(String name);
    void setOnlineStatus(Long userId, boolean online);
    List<User> getOnlineUsers();
    String register(String username, String password);
    LoginResponse login(String username, String password);
}

package com.gary.ChatApp.service;

import com.gary.ChatApp.domain.model.user.User;

import java.util.Optional;
import java.util.List;

public interface UserService {
    Optional<User> getById(Long id);
    Optional<User> getByName(String name);
    void setOnlineStatus(Long userId, boolean online);
    List<User> getOnlineUsers();
}

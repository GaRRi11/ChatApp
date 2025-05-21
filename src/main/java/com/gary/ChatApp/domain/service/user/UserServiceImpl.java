package com.gary.ChatApp.service.impl;

import com.gary.ChatApp.domain.model.user.User;
import com.gary.ChatApp.repository.UserRepository;
import com.gary.ChatApp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Optional<User> getById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> getByName(String name) {
        return userRepository.findByName(name);
    }

    @Override
    public void setOnlineStatus(Long userId, boolean online) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setOnline(online);
            userRepository.save(user);
        });
    }

    @Override
    public List<User> getOnlineUsers() {
        return userRepository.findByOnlineTrue();
    }
}

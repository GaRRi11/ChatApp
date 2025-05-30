package com.gary.ChatApp.domain.service.user;

import com.gary.ChatApp.domain.model.user.User;
import com.gary.ChatApp.web.dto.LoginResponse;
import com.gary.ChatApp.web.dto.user.UserRequest;
import com.gary.ChatApp.web.dto.user.UserResponse;

import java.util.Optional;
import java.util.List;

public interface UserService {
    Optional<User> getById(Long id);
    UserResponse register(UserRequest userRequest);
    LoginResponse refreshToken(String token);
    LoginResponse login(UserRequest userRequest);
    List<User> findAllById(List<Long> userIds);
}

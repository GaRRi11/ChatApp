package com.gary.domain.service.user;

import com.gary.domain.model.user.User;
import com.gary.web.dto.loginResponse.LoginResponseDto;
import com.gary.web.dto.user.UserRequest;
import com.gary.web.dto.user.UserResponse;

import java.util.Optional;
import java.util.List;

public interface UserService {
    Optional<User> getById(Long id);
    UserResponse register(UserRequest userRequest);
    LoginResponseDto refreshToken(String token);
    LoginResponseDto login(UserRequest userRequest);
    List<User> findAllById(List<Long> userIds);
}

package com.gary.domain.service.user;

import com.gary.domain.model.user.User;
import com.gary.web.dto.rest.loginResponse.LoginResponseDto;
import com.gary.web.dto.rest.user.UserRequest;
import com.gary.web.dto.rest.user.UserResponse;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface UserService {
    Optional<User> findById(UUID id);

    UserResponse register(UserRequest userRequest);

    LoginResponseDto refreshToken(String token);

    LoginResponseDto login(UserRequest userRequest);

    void logout(UUID userId);

    List<User> findAllById(List<UUID> userIds);

    List<UserResponse> searchByUsername(String username);
}

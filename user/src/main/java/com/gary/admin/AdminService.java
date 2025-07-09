package com.gary.admin;

import com.gary.web.dto.rest.user.UserResponse;

import java.util.List;
import java.util.UUID;

public interface AdminService {

    List<UserResponse> getAllUsers();

    UserResponse getUserById(UUID id);

    void deleteUser(UUID id);
}


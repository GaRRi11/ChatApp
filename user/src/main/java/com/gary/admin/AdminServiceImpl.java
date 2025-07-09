package com.gary.admin;

import com.gary.domain.model.user.User;
import com.gary.web.dto.rest.user.UserResponse;
import com.gary.web.exception.rest.ResourceNotFoundException;
import com.gary.application.user.UserTransactionHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserTransactionHelper userTransactionHelper;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userTransactionHelper.findAll().stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        return userTransactionHelper.findById(id)
                .map(UserResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        User user = userTransactionHelper.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userTransactionHelper.delete(user.getId());
    }
}


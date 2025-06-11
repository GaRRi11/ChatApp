package com.gary.web.controller.user;

import com.gary.domain.model.user.User;
import com.gary.domain.service.user.UserService;
import com.gary.web.dto.SearchUser.SearchUserRequest;
import com.gary.web.dto.user.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserSearchController {

    private final UserService userService;

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserResponse>> searchUsers(
            @RequestParam @Valid SearchUserRequest searchUserRequest,
            @AuthenticationPrincipal User authenticatedUser) {

        List<UserResponse> results = userService.searchByUsername(searchUserRequest.username(), authenticatedUser.getId());
        return ResponseEntity.ok(results);
    }
}

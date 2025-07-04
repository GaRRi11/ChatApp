package com.gary.web.controller.user;

import com.gary.domain.model.user.User;
import com.gary.domain.service.user.UserService;
import com.gary.web.dto.rest.user.UserResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserSearchController {

    private final UserService userService;

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserResponse>> searchUsers(
            @RequestParam
            @NotBlank(message = "Username is required")
            @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
            String username,
            @AuthenticationPrincipal User authenticatedUser) {


        if (username.equalsIgnoreCase(authenticatedUser.getUsername())) {
            return ResponseEntity.noContent().build();
        }

        List<UserResponse> results = userService.searchByUsername(username);

        if (results.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(results);
    }
}

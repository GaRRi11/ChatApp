package com.gary.ChatApp.web.controller;

import com.gary.ChatApp.domain.model.user.User;
import com.gary.ChatApp.domain.service.friendship.FriendshipService;
import com.gary.ChatApp.web.dto.user.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@Slf4j
public class FriendshipController {

    private final FriendshipService friendshipService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getFriends(@AuthenticationPrincipal User authenticatedUser) {
        Long userId = authenticatedUser.getId();

        log.debug("Fetching friends for userId={}", userId);
        List<UserResponse> friends = friendshipService.getFriends(userId);
        return ResponseEntity.ok(friends);
    }

    @DeleteMapping("/remove/{friendId}")
    public ResponseEntity<Void> removeFriend(@AuthenticationPrincipal User authenticatedUser,
                                             @PathVariable Long friendId) {
        Long userId = authenticatedUser.getId();

        if (friendId == null || friendId <= 0) {
            log.warn("Invalid friendId received in removeFriend: {}", friendId);
            return ResponseEntity.badRequest().build();
        }

        log.debug("User {} removing friend {}", userId, friendId);
        friendshipService.removeFriend(userId, friendId);
        return ResponseEntity.noContent().build();
    }
}

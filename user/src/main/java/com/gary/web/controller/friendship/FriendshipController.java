package com.gary.web.controller.friendship;

import com.gary.domain.model.user.User;
import com.gary.domain.service.friendship.FriendshipService;
import com.gary.web.dto.user.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@Slf4j
public class FriendshipController {

    private final FriendshipService friendshipService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserResponse>> getFriends(@AuthenticationPrincipal User authenticatedUser) {
        UUID userId = authenticatedUser.getId();

        log.debug("Fetching friends for userId={}", userId);
        List<UserResponse> friends = friendshipService.getFriends(userId);
        return ResponseEntity.ok(friends);
    }

    @DeleteMapping("/remove/{friendId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeFriend(@AuthenticationPrincipal User authenticatedUser,
                                             @PathVariable UUID friendId) {
        UUID userId = authenticatedUser.getId();

        if (friendId == null) {
            log.warn("Invalid friendId received in removeFriend: {}", friendId);
            return ResponseEntity.badRequest().build();
        }

        log.debug("User {} removing friend {}", userId, friendId);
        friendshipService.removeFriend(userId, friendId);
        return ResponseEntity.noContent().build();
    }
}

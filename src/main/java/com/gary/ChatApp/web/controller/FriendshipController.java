package com.gary.ChatApp.web.controller;

import com.gary.ChatApp.domain.service.friendship.FriendshipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@Slf4j
public class FriendshipController {

    private final FriendshipService friendshipService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<Long>> getFriends(@PathVariable Long userId) {
        if (userId == null || userId <= 0) {
            log.warn("Invalid userId received in getFriends: {}", userId);
            return ResponseEntity.badRequest().build();
        }
        List<Long> friends = friendshipService.getFriendIds(userId);
        return ResponseEntity.ok(friends);
    }

    @DeleteMapping("/{userId}/remove/{friendId}")
    public ResponseEntity<Void> removeFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        if (userId == null || friendId == null || userId <= 0 || friendId <= 0) {
            log.warn("Invalid userId or friendId received in removeFriend: userId={}, friendId={}", userId, friendId);
            return ResponseEntity.badRequest().build();
        }
        friendshipService.removeFriend(userId, friendId);
        return ResponseEntity.noContent().build();
    }
}

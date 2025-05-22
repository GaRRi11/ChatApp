package com.gary.ChatApp.web.controller;

import com.gary.ChatApp.domain.service.friendship.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    @GetMapping("/{userId}")
    public List<Long> getFriends(@PathVariable Long userId) {
        return friendshipService.getFriendIds(userId);
    }

    @DeleteMapping("/{userId}/remove/{friendId}")
    public void removeFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        friendshipService.removeFriend(userId, friendId);
    }
}

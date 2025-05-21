package com.gary.ChatApp.web.controller;

import com.gary.ChatApp.service.FriendshipService;
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

    @DeleteMapping
    public void removeFriend(@RequestParam Long userId, @RequestParam Long friendId) {
        friendshipService.removeFriend(userId, friendId);
    }
}

package com.gary.ChatApp.controller;

import com.gary.ChatApp.domain.model.friendrequest.FriendRequest;
import com.gary.ChatApp.service.FriendRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friend-requests")
@RequiredArgsConstructor
public class FriendRequestController {

    private final FriendRequestService friendRequestService;

    @PostMapping
    public FriendRequest sendRequest(@RequestParam Long senderId, @RequestParam Long receiverId) {
        return friendRequestService.sendRequest(senderId, receiverId);
    }

    @PostMapping("/{id}/respond")
    public void respondToRequest(@PathVariable Long id, @RequestParam boolean accept) {
        friendRequestService.respondToRequest(id, accept);
    }

    @GetMapping("/pending/{userId}")
    public List<FriendRequest> getPendingRequests(@PathVariable Long userId) {
        return friendRequestService.getPendingRequests(userId);
    }
}

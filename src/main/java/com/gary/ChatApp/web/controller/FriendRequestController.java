package com.gary.ChatApp.web.controller;

import com.gary.ChatApp.domain.model.friendrequest.FriendRequest;
import com.gary.ChatApp.domain.service.friendRequest.FriendRequestService;
import com.gary.ChatApp.domain.service.friendship.FriendshipService;
import com.gary.ChatApp.exceptions.FriendshipAlreadyExistsException;
import com.gary.ChatApp.web.dto.FriendRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friend-requests")
@RequiredArgsConstructor
public class FriendRequestController {

    private final FriendRequestService friendRequestService;
    private final FriendshipService friendshipService;

    @PostMapping
    public FriendRequestDto sendRequest(@RequestBody FriendRequestDto request) {
        if (friendshipService.areFriends(request.getSenderId(), request.getReceiverId())) {
            throw new FriendshipAlreadyExistsException(request.getSenderId(), request.getReceiverId());
        }
        return friendRequestService.sendRequest(request.getSenderId(), request.getReceiverId());
    }

    @PostMapping("/{id}/respond")
    public void respondToRequest(@PathVariable Long id, @RequestParam boolean accept) {
        friendRequestService.respondToRequest(id, accept);
    }

    @GetMapping("/pending/{userId}")
    public List<FriendRequestDto> getPendingRequests(@PathVariable Long userId) {
        return friendRequestService.getPendingRequests(userId);
    }
}

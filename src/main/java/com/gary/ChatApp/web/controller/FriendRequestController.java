package com.gary.ChatApp.web.controller;

import com.gary.ChatApp.domain.model.friendrequest.FriendRequest;
import com.gary.ChatApp.domain.service.friendRequest.FriendRequestService;
import com.gary.ChatApp.domain.service.friendship.FriendshipService;
import com.gary.ChatApp.exceptions.FriendshipAlreadyExistsException;
import com.gary.ChatApp.web.dto.FriendRequestDto;
import com.gary.ChatApp.web.dto.RespondToFriendRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friend-requests")
@RequiredArgsConstructor
@Slf4j
public class FriendRequestController {

    private final FriendRequestService friendRequestService;
    private final FriendshipService friendshipService;

    @PostMapping("/send")
    public FriendRequestDto sendRequest(@RequestBody FriendRequestDto request) {
        if (friendshipService.areFriends(request.senderId(), request.receiverId())) {
            throw new FriendshipAlreadyExistsException(request.senderId(), request.receiverId());
        }
        return friendRequestService.sendRequest(request);
    }

    @PostMapping("/{id}/respond")
    public void respondToRequest(@RequestBody @Valid RespondToFriendRequestDto responseDto) {
        log.debug("Received respondToRequest for requestId={}, accept={}", responseDto.requestId(), responseDto.accept());
        friendRequestService.respondToRequest(responseDto);
    }

    @GetMapping("/pending/{userId}")
    public List<FriendRequestDto> getPendingRequests(@PathVariable Long userId) {
        return friendRequestService.getPendingRequests(userId);
    }
}

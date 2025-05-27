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
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<FriendRequestDto> sendRequest(@RequestBody @Valid FriendRequestDto request) {
        log.debug("Received sendRequest from user {} to {}", request.senderId(), request.receiverId());

        if (friendshipService.areFriends(request.senderId(), request.receiverId())) {
            log.warn("Users {} and {} are already friends", request.senderId(), request.receiverId());
            throw new FriendshipAlreadyExistsException(request.senderId(), request.receiverId());
        }

        FriendRequestDto sentRequest = friendRequestService.sendRequest(request);
        return ResponseEntity.ok(sentRequest);
    }

    @PostMapping("/respond")
    public ResponseEntity<Void> respondToRequest(@RequestBody @Valid RespondToFriendRequestDto responseDto) {
        log.debug("Received respondToRequest for requestId={}, accept={}", responseDto.requestId(), responseDto.accept());
        friendRequestService.respondToRequest(responseDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pending/{userId}")
    public ResponseEntity<List<FriendRequestDto>> getPendingRequests(@PathVariable Long userId) {
        log.debug("Fetching pending friend requests for userId={}", userId);
        List<FriendRequestDto> pendingRequests = friendRequestService.getPendingRequests(userId);
        return ResponseEntity.ok(pendingRequests);
    }
}

package com.gary.ChatApp.web.controller;

import com.gary.ChatApp.service.friendRequest.FriendRequestService;
import com.gary.ChatApp.service.user.UserService;
import com.gary.ChatApp.storage.model.friendrequest.FriendRequest;
import com.gary.ChatApp.storage.model.user.User;
import com.gary.ChatApp.web.dto.FriendRequestDTO;
import com.gary.ChatApp.web.dto.FriendRequestDTOMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/friend")
@AllArgsConstructor
public class FriendRequestController {

    private final UserService userService;
    private final FriendRequestService friendRequestService;

    private final FriendRequestDTOMapper friendRequestDTOMapper;



    @PostMapping("/send")
    public ResponseEntity<String> sendFriendRequest(FriendRequestDTO friendRequestDTO) {
        // Check if sender and receiver exist
        User sender = userService.findById(friendRequestDTO.getSenderId()).get();   //TODO mivxedav
        User receiver = userService.findById(friendRequestDTO.getReceiverId()).get();

        if (sender == null || receiver == null) {
            return ResponseEntity.badRequest().body("Invalid sender or receiver ID");
        }

        // Send friend request
        friendRequestService.sendFriendRequest(friendRequestDTOMapper.fromDTO(friendRequestDTO));
        return ResponseEntity.ok("Friend request sent successfully");
    }

    @PostMapping("/accept")
    public ResponseEntity<String> acceptFriendRequest(@RequestParam("requestId") Long requestId) {
        // Fetch friend request by ID
        FriendRequest friendRequest = friendRequestService.findById(requestId).get(); //TODO mivxedav

        if (friendRequest == null) {
            return ResponseEntity.badRequest().body("Friend request not found");
        }

        // Accept the friend request
        friendRequestService.acceptFriendRequest(friendRequest);
        return ResponseEntity.ok("Friend request accepted");
    }

    @PostMapping("/decline")
    public ResponseEntity<String> declineFriendRequest(@RequestParam("requestId") Long requestId) {
        // Fetch friend request by ID
        FriendRequest friendRequest = friendRequestService.findById(requestId).get(); //TODO mivxedav

        if (friendRequest == null) {
            return ResponseEntity.badRequest().body("Friend request not found");
        }

        // Decline the friend request
        friendRequestService.declineFriendRequest(friendRequest);
        return ResponseEntity.ok("Friend request declined");
    }
}

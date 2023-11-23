package com.gary.ChatApp.web.dto;

import com.gary.ChatApp.storage.model.friendrequest.FriendRequest;
import org.springframework.stereotype.Component;

@Component
public class FriendRequestDTOMapper {
    public FriendRequest fromDTO(FriendRequestDTO friendRequestDTO){
        return new FriendRequest(
                friendRequestDTO.getSenderId(),
                friendRequestDTO.getReceiverId()
        );
    }
}

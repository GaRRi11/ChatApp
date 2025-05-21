package com.gary.ChatApp.web.dto;

import com.gary.ChatApp.domain.model.friendrequest.FriendRequest;
import org.springframework.stereotype.Component;

@Component
public class FriendRequestDTOMapper {
    public FriendRequest fromDTO(Long senderId, Long receiverId){
        return new FriendRequest(
                senderId,
                receiverId
        );
    }
}

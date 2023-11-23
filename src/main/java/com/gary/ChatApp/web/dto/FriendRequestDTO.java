package com.gary.ChatApp.web.dto;

import lombok.Data;

@Data
public class FriendRequestDTO {
    private Long senderId;
    private Long receiverId;

    public FriendRequestDTO(Long senderId, Long receiverId) {
        this.senderId = senderId;
        this.receiverId = receiverId;
    }
}

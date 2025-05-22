package com.gary.ChatApp.web.dto;

import lombok.Data;

@Data
public class FriendRequestDto {
    private Long senderId;
    private Long receiverId;
    private String status; // Optional


}


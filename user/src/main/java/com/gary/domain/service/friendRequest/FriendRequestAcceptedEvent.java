package com.gary.domain.service.friendRequest;


import org.springframework.context.ApplicationEvent;

public class FriendRequestAcceptedEvent extends ApplicationEvent {

    private final Long senderId;
    private final Long receiverId;

    public FriendRequestAcceptedEvent(Object source, Long senderId, Long receiverId) {
        super(source);
        this.senderId = senderId;
        this.receiverId = receiverId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }
}


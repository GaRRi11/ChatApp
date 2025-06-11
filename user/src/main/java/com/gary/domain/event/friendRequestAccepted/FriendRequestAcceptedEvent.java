package com.gary.domain.event.friendRequestAccepted;


import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FriendRequestAcceptedEvent extends ApplicationEvent {

    private final Long senderId;
    private final Long receiverId;

    public FriendRequestAcceptedEvent(Object source, Long senderId, Long receiverId) {
        super(source);
        this.senderId = senderId;
        this.receiverId = receiverId;
    }

}


package com.gary.domain.event.friendRequestAccepted;


import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class FriendRequestAcceptedEvent extends ApplicationEvent {

    private final UUID senderId;
    private final UUID receiverId;

    public FriendRequestAcceptedEvent(Object source, UUID senderId, UUID receiverId) {
        super(source);
        this.senderId = senderId;
        this.receiverId = receiverId;
    }

}


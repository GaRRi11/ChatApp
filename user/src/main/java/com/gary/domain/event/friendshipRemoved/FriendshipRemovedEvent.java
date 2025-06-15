package com.gary.domain.event.friendshipRemoved;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class FriendshipRemovedEvent extends ApplicationEvent {

    private final UUID userId;
    private final UUID friendId;

    public FriendshipRemovedEvent(Object source, UUID userId, UUID friendId) {
        super(source);
        this.userId = userId;
        this.friendId = friendId;
    }

}


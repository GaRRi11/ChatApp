package com.gary.domain.event.friendshipRemoved;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FriendshipRemovedEvent extends ApplicationEvent {

    private final Long userId;
    private final Long friendId;

    public FriendshipRemovedEvent(Object source, Long userId, Long friendId) {
        super(source);
        this.userId = userId;
        this.friendId = friendId;
    }

}


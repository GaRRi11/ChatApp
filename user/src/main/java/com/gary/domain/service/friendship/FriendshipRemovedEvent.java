package com.gary.domain.service.friendship;

import org.springframework.context.ApplicationEvent;

public class FriendshipRemovedEvent extends ApplicationEvent {

    private final Long userId;
    private final Long friendId;

    public FriendshipRemovedEvent(Object source, Long userId, Long friendId) {
        super(source);
        this.userId = userId;
        this.friendId = friendId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getFriendId() {
        return friendId;
    }
}


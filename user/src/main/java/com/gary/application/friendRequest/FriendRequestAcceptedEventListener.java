package com.gary.application.friendRequest;

import com.gary.domain.event.friendRequestAccepted.FriendRequestAcceptedEvent;
import com.gary.domain.repository.friendship.FriendshipRepository;
import com.gary.application.friendship.FriendshipManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FriendRequestAcceptedEventListener {

    private final FriendshipManager friendshipManager;

    @EventListener
    public void handleFriendRequestAccepted(FriendRequestAcceptedEvent event) {
        friendshipManager.saveBidirectional(event.getSenderId(), event.getReceiverId());
        log.info("Friendship created via event for sender={} and receiver={}", event.getSenderId(), event.getReceiverId());
    }
}


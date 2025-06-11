package com.gary.domain.service.friendRequest;

import com.gary.domain.repository.FriendshipRepository;
import com.gary.domain.service.friendship.FriendshipManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FriendRequestAcceptedEventListener {

    private final FriendshipManager friendshipManager;
    private final FriendshipRepository friendshipRepository;

    @EventListener
    public void handleFriendRequestAccepted(FriendRequestAcceptedEvent event) {
        friendshipManager.saveBidirectional(event.getSenderId(), event.getReceiverId(), friendshipRepository);
        log.info("Friendship created via event for sender={} and receiver={}", event.getSenderId(), event.getReceiverId());
    }
}

